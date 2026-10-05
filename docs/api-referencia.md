# Referência da API

Lista completa dos endpoints expostos pelo backend, extraída dos controllers em
`src/main/java/com/app/fisiotech/*/controller`. O Swagger UI (`/swagger-ui.html`) gera a
mesma lista a partir do código em execução; este documento acrescenta o que o Swagger não
mostra: quem pode chamar cada rota, as regras de negócio e os erros esperados.

- [Convenções](#convenções)
- [Públicos](#públicos)
- [Qualquer usuário autenticado](#qualquer-usuário-autenticado)
- [Administrador](#administrador-role_admin)
- [Profissional](#profissional-role_profissional)
- [Paciente](#paciente-role_paciente)
- [Infraestrutura](#infraestrutura)

---

## Convenções

### Autenticação

Rotas protegidas exigem `Authorization: Bearer <accessToken>`, obtido em `POST /auth/login`.
O papel do usuário é verificado pelo prefixo da rota (`SecurityConfig`); papel errado retorna
**403**. Detalhes do token, sessão e renovação em [Autenticação JWT](autenticacao-jwt.md).

| Prefixo | Papel exigido |
|---|---|
| `/profissionais/me/**` | `PROFISSIONAL` |
| `/profissionais/**`, `/admin/pacientes/**`, `/admin/me/**` | `ADMIN` |
| `/pacientes/**`, `/consultas/**`, `/mensagens/**`, `/avaliacoes/**`, `/prontuario/**`, `/agenda/**` | `PROFISSIONAL` |
| `/me/**` | `PACIENTE` |
| `/auth/me` e qualquer outra rota não listada | qualquer usuário autenticado |

Exceções públicas (somente `POST`): `/pacientes/cadastro`, `/auth/login`, `/auth/refresh`,
`/auth/logout`, `/auth/recuperar-senha`, `/auth/redefinir-senha`. Também são públicos o Swagger
(`/swagger-ui/**`, `/swagger-ui.html`, `/v3/api-docs/**`), `/actuator/health/**` e
`/actuator/info`. O console `/h2-console/**` só abre com o profile `dev` ativo e `prod` inativo.

> Um Bearer inválido é rejeitado mesmo em rota pública. O app deve chamar login, refresh,
> logout, cadastro e recuperação de senha **sem** o cabeçalho `Authorization`.

### Isolamento por dono (404, não 403)

Profissional e paciente só enxergam os próprios recursos. Pedir um recurso que existe mas
pertence a outra pessoa devolve **404** com a mesma mensagem de "não encontrado", para não
confirmar a existência do registro. Isso vale para pacientes, consultas, avaliações,
prontuário e mensagens.

### Formato de datas

- `LocalDateTime`: ISO-8601 sem fuso, ex. `2026-08-20T09:00:00`.
- `LocalDate`: `2026-08-20`. `LocalTime`: `09:00:00`.
- O servidor não converte fusos; a data é gravada como enviada.

### Erros

Erros de regra de negócio e validação (`ApiExceptionHandler`):

```json
{
  "timestamp": "2026-08-20T09:00:00.123",
  "status": 409,
  "error": "Conflict",
  "message": "Este horário não está mais disponível."
}
```

Erros de autenticação/autorização gerados pelo filtro de segurança não têm `timestamp`:

```json
{"status": 401, "error": "Unauthorized", "message": "Token inválido ou expirado."}
```

Em `dev`, um corpo que não é JSON válido (ou não está em UTF-8) gera um 400 no formato
padrão do Spring, com o campo `trace` contendo a stack trace — comportamento do DevTools,
que não está presente no jar empacotado.

| Status | Quando |
|---|---|
| 400 | Corpo inválido (`@Valid`) — a mensagem é sempre genérica: `"Dados da requisição inválidos."`, sem indicar o campo; senha atual incorreta; código de recuperação inválido; transição de estado inválida (ex. cancelar consulta já realizada, avaliar consulta não realizada, período com fim antes do início) |
| 401 | Token ausente, inválido, expirado ou de sessão revogada; credenciais erradas no login |
| 403 | Papel sem permissão para o prefixo da rota |
| 404 | Recurso inexistente **ou** de outro dono |
| 409 | Email ou registro profissional já usado; horário ocupado; avaliação duplicada; exclusão bloqueada por registros associados (chave estrangeira) |

### Paginação

As rotas `/paginado` recebem `page` (a partir de 0, padrão `0`), `size` (padrão `20`, limitado
a `1..100`), `sort` e `direction` (`asc` padrão, ou `desc`). Um `sort` fora da lista permitida
da rota é trocado silenciosamente por `id`. Resposta (`PageResponse`):

```json
{
  "content": [ ... ],
  "page": 0,
  "size": 20,
  "totalElements": 42,
  "totalPages": 3,
  "first": true,
  "last": false
}
```

### Enumerações

| Enum | Valores |
|---|---|
| `TipoConsulta` | `PRESENCIAL`, `ONLINE` |
| `StatusConsulta` | `AGENDADA`, `CONFIRMADA`, `REALIZADA`, `CANCELADA` |
| `AutorMensagem` | `PROFISSIONAL`, `PACIENTE` |
| `StatusPlano` | `ATIVO`, `SUBSTITUIDO`, `ENCERRADO` |

### Validações comuns de cadastro

| Campo | Regra |
|---|---|
| `nome` | obrigatório, até 120 caracteres |
| `email` | obrigatório, formato de email, até 120 caracteres; gravado em minúsculas e sem espaços |
| `senha` (criação) | obrigatória, 8 a 100 caracteres |
| `senha` (edição) | opcional: **omita o campo ou envie `null`** para manter a senha. String vazia (`""`) é rejeitada com 400. Se enviada, 8 a 100 caracteres e encerra as sessões da conta |

**Os `PUT` de edição substituem o registro inteiro.** Campo opcional omitido é gravado como
nulo — por exemplo, editar um profissional sem enviar `conveniosAceitos` apaga a lista de
convênios. O cliente deve reenviar todos os campos que quer manter (a exceção é `senha`, que
omitida mantém a atual).

O email é único **entre os três tipos de conta** (tabela `auth_emails`): um paciente não pode
usar o email de um profissional ou do admin. Conflito retorna 409.

---

## Públicos

### `POST /auth/login`

```json
{"email": "admin@fisiotech.com", "senha": "12345678"}
```

**200**, com `Cache-Control: no-store`:

```json
{
  "accessToken": "eyJ...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "refreshToken": "valor-aleatorio",
  "refreshExpiresIn": 604800
}
```

Validades em segundos. Credenciais erradas: **401**, sem distinguir email inexistente de senha
incorreta. Se o mesmo email existisse em mais de um tipo de conta (só possível em bases
legadas), a precedência de busca é profissional → admin → paciente.

### `POST /auth/refresh`

`{"refreshToken": "..."}` → **200** no mesmo formato do login. O refresh token anterior é
consumido. Reutilizar um token já consumido revoga a sessão inteira e retorna **401**.

### `POST /auth/logout`

`{"refreshToken": "..."}` → **204** sempre (inclusive token desconhecido ou repetido). Revoga
apenas a sessão daquele token.

### `POST /auth/recuperar-senha`

`{"email": "..."}` → **204** sempre, exista ou não a conta. Se existir, gera um código de
6 dígitos válido por 15 minutos (`app.recuperacao-senha.validade-minutos`), invalida qualquer
código anterior do mesmo email e envia por SMTP. No profile `dev` o código só aparece no log:
`[DEV] Código de recuperação de senha para <email>: <código>`.

### `POST /auth/redefinir-senha`

```json
{"email": "joao@paciente.com", "codigo": "123456", "novaSenha": "outraSenha123"}
```

`codigo` deve ter exatamente 6 dígitos; `novaSenha`, 8 a 100 caracteres. **204** em caso de
sucesso: troca a senha, marca o código como usado e encerra todas as sessões da conta.
Código errado, expirado, já usado ou com 5 tentativas erradas: **400**
`"Código inválido ou expirado."` — é preciso pedir um código novo.

### `POST /pacientes/cadastro`

Autocadastro do paciente. Corpo: `nome`, `email`, `senha`. **201** com `Location`. O paciente
nasce **sem profissional responsável**; o vínculo é criado no primeiro agendamento
(`POST /me/consultas`). Email já usado: **409**.

---

## Qualquer usuário autenticado

### `GET /auth/me`

**200** `{"id": 1, "nome": "Ana Souza", "email": "ana@fisiotech.com", "role": "ROLE_PROFISSIONAL"}`.
Os dados são lidos da conta atual a cada chamada.

---

## Administrador (`ROLE_ADMIN`)

### Profissionais

| Método e rota | Descrição | Resposta |
|---|---|---|
| `POST /profissionais` | Cadastra profissional | 201 + `Location` |
| `GET /profissionais` | Lista todos | 200 `ProfissionalResponse[]` |
| `GET /profissionais/paginado` | Filtros `nome`, `especialidade` (contém, sem diferenciar maiúsculas); `sort` ∈ `id`, `nome`, `email`, `especialidade`, `valorConsultaParticular` | 200 `PageResponse` |
| `GET /profissionais/{id}` | Detalhe | 200 |
| `PUT /profissionais/{id}` | Atualiza (senha opcional) | 204 |
| `DELETE /profissionais/{id}` | Exclui conta e revoga sessões | 204; 409 se houver pacientes/consultas vinculados |

Corpo de criação/edição:

```json
{
  "nome": "Ana Souza",
  "email": "ana@fisiotech.com",
  "senha": "senha123",
  "registroProfissional": "CREFITO-11111",
  "especialidade": "Ortopedia",
  "valorConsultaParticular": 150.00,
  "conveniosAceitos": ["Unimed", "Amil"],
  "foto": null,
  "dataNascimento": "1990-05-10",
  "sexo": "F",
  "telefone": "11999990000"
}
```

`registroProfissional` é obrigatório (até 20 caracteres) e único; `especialidade` é
obrigatória (até 120); `valorConsultaParticular` não pode ser negativo.

### Pacientes (visão global)

| Método e rota | Descrição | Resposta |
|---|---|---|
| `GET /admin/pacientes` | Todos os pacientes, de todos os profissionais | 200 |
| `GET /admin/pacientes/paginado` | `filtro` busca em nome **ou** email; `sort` ∈ `id`, `nome`, `email` | 200 `PageResponse` |
| `GET /admin/pacientes/{id}` | Detalhe | 200 |
| `PUT /admin/pacientes/{id}` | Edita `nome`, `email`, `senha` (opcional) e **`profissionalId` (obrigatório)** — permite transferir o paciente | 204 |

### Conta do admin

| Método e rota | Corpo | Resposta |
|---|---|---|
| `PUT /admin/me/senha` | `{"senhaAtual": "...", "novaSenha": "..."}` | 204; 400 se a senha atual estiver errada. Encerra todas as sessões do admin |

---

## Profissional (`ROLE_PROFISSIONAL`)

### Conta

| Método e rota | Corpo | Resposta |
|---|---|---|
| `PUT /profissionais/me/senha` | `{"senhaAtual": "...", "novaSenha": "..."}` | 204; encerra todas as sessões |

### Pacientes

Somente pacientes cujo profissional responsável é o usuário logado.

| Método e rota | Descrição | Resposta |
|---|---|---|
| `POST /pacientes` | Cadastra paciente já vinculado ao profissional logado (`nome`, `email`, `senha`) | 201 + `Location` |
| `GET /pacientes` | Lista os próprios pacientes, ordenados por `id` | 200 |
| `GET /pacientes/paginado` | `filtro` em nome ou email; `sort` ∈ `id`, `nome`, `email` | 200 `PageResponse` |
| `GET /pacientes/{id}` | Detalhe | 200 |
| `PUT /pacientes/{id}` | Edita dados cadastrais (senha opcional; `dataNascimento`, `sexo`, `profissao`, `telefone`, `endereco`, `bairro`, `foto` opcionais) | 204 |
| `DELETE /pacientes/{id}` | Exclui conta e revoga sessões | 204; 409 se houver consultas/mensagens associadas |

### Consultas

| Método e rota | Descrição | Resposta |
|---|---|---|
| `POST /consultas` | Agenda para um paciente próprio | 201 + `Location`; 409 se horário ocupado |
| `GET /consultas?pacienteId=` | Lista as consultas do profissional (filtro opcional), mais recentes primeiro | 200 `ConsultaResponse[]` |
| `GET /consultas/{id}` | Detalhe, inclusive o registro clínico | 200 |
| `PUT /consultas/{id}` | Atualiza data, tipo, **status**, convênio, valor e registro clínico | 204 |
| `DELETE /consultas/{id}` | Exclui a consulta e sua avaliação | 204; 409 se houver evolução clínica ligada a ela |

Criação:

```json
{"pacienteId": 1, "dataHora": "2026-08-20T09:00:00", "tipo": "PRESENCIAL", "convenio": null, "valor": 150.00}
```

Atualização (`PUT /consultas/{id}`) — `dataHora`, `tipo` e `status` são obrigatórios. Os
quatro blocos clínicos são opcionais; bloco omitido mantém o valor gravado, bloco enviado
substitui o bloco inteiro:

```json
{
  "dataHora": "2026-08-20T09:00:00",
  "tipo": "PRESENCIAL",
  "status": "REALIZADA",
  "convenio": null,
  "valor": 150.00,
  "quadroClinico": {
    "queixaPrincipal": "Dor lombar", "historiaDoencaAtual": "...", "historicoSaude": "...",
    "cirurgias": false, "cirurgiasDescricao": null,
    "lesoesAnteriores": true, "lesoesAnterioresDescricao": "...", "medicamentos": "..."
  },
  "habitosVida": {"atividadeFisica": "...", "rotinaTrabalho": "...", "tabagismo": false, "consumoAlcool": false},
  "exameFisico": {"postura": "...", "amplitudeMovimento": "...", "palpacao": "...", "forcaMuscular": "..."},
  "diagnostico": {"planoTratamento": "...", "objetivosTratamento": "..."}
}
```

Textos clínicos aceitam até 2000 caracteres (`historicoSaude`: 255). Mudar `dataHora` verifica
conflito de horário e marca `foiRemarcada = true`. O profissional pode definir qualquer status,
inclusive reabrir uma consulta cancelada.

### Agenda e indicadores

| Método e rota | Parâmetros | Resposta |
|---|---|---|
| `GET /agenda` | `inicio`, `fim` (obrigatórios, `LocalDateTime`); `pacienteId`, `status`, `tipo` opcionais | 200 `ConsultaResponse[]` do período; 400 se `fim` < `inicio` |
| `GET /agenda/indicadores` | `inicio`, `fim` | 200 |

```json
{
  "totalConsultas": 12,
  "pacientesAtendidos": 7,
  "taxaCancelamento": 16.67,
  "consultasPorStatus": {"AGENDADA": 3, "CONFIRMADA": 1, "REALIZADA": 6, "CANCELADA": 2}
}
```

`pacientesAtendidos` conta pacientes distintos com consulta `REALIZADA` no período;
`taxaCancelamento` é a porcentagem de `CANCELADA` sobre o total, com duas casas.

### Mensagens

| Método e rota | Descrição | Resposta |
|---|---|---|
| `POST /mensagens` | `{"pacienteId": 1, "autor": "PROFISSIONAL", "conteudo": "..."}` (até 2000 caracteres) | 201 + `Location` |
| `GET /mensagens?pacienteId=` | Conversa com o paciente, em ordem cronológica | 200 `MensagemResponse[]` |
| `GET /mensagens/caixa-entrada` | Um item por paciente com quem há consulta: conversas com mensagem primeiro (mais recente no topo), depois as sem mensagem (por nome) | 200 |

Só existe conversa se houver **pelo menos uma consulta** (qualquer status, inclusive cancelada)
entre o paciente e o profissional; sem isso, **404**. Ter cadastrado o paciente não basta.

### Avaliações

| Método e rota | Descrição | Resposta |
|---|---|---|
| `POST /avaliacoes` | `{"consultaId": 1, "nota": 5, "comentario": "..."}` — nota 1 a 5, comentário até 1000 | 201; 400 se a consulta não estiver `REALIZADA`; 409 se já avaliada |
| `GET /avaliacoes/consulta/{consultaId}` | Avaliação da consulta | 200; 404 se ainda não avaliada |

### Prontuário

| Método e rota | Descrição | Resposta |
|---|---|---|
| `POST /prontuario/pacientes/{pacienteId}/evolucoes` | Registra evolução clínica | 201 `EvolucaoResponse` |
| `GET /prontuario/pacientes/{pacienteId}/evolucoes` | Evoluções do paciente feitas por este profissional, mais recentes primeiro | 200 |
| `POST /prontuario/pacientes/{pacienteId}/planos` | Cria nova revisão do plano terapêutico | 201 `PlanoResponse` |
| `GET /prontuario/pacientes/{pacienteId}/planos` | Revisões do plano, da mais nova para a mais antiga | 200 |

Evolução:

```json
{
  "consultaId": 3,
  "observacoes": "Paciente relata melhora da dor",
  "procedimentos": "Cinesioterapia, TENS",
  "respostaPaciente": "Boa tolerância",
  "conduta": "Manter protocolo",
  "visivelPaciente": true
}
```

`observacoes` é obrigatório (até 4000); `procedimentos` até 4000; `respostaPaciente` e
`conduta` até 2000; `consultaId` é opcional e, se enviado, precisa ser uma consulta deste
paciente com este profissional. Não há endpoint de edição nem de exclusão: a evolução é
um registro imutável.

Plano terapêutico:

```json
{
  "objetivos": "Reduzir dor e recuperar amplitude",
  "condutas": "Fortalecimento de core 2x/semana",
  "dataInicio": "2026-08-20",
  "dataFimPrevista": "2026-10-20",
  "visivelPaciente": true
}
```

Cada `POST` cria a revisão seguinte (`revisao` 1, 2, 3…); a revisão anterior que estava
`ATIVO` passa a `SUBSTITUIDO`. Só existe um plano ativo por paciente e profissional.
`dataFimPrevista` anterior a `dataInicio` retorna 400.

---

## Paciente (`ROLE_PACIENTE`)

### Perfil

| Método e rota | Descrição | Resposta |
|---|---|---|
| `GET /me` | Próprio perfil | 200 `PacienteResponse` |
| `PUT /me` | Edita `nome`, `email` (obrigatórios) e `dataNascimento`, `sexo`, `profissao`, `telefone`, `endereco`, `bairro`, `foto` | 204 |
| `PUT /me/senha` | `{"senhaAtual": "...", "novaSenha": "..."}` | 204; encerra todas as sessões |

### Busca de profissionais e horários

| Método e rota | Descrição | Resposta |
|---|---|---|
| `GET /me/profissionais?nome=&especialidade=` | Todos os profissionais, filtro "contém" opcional, por nome | 200 `ProfissionalPublicoResponse[]` |
| `GET /me/profissionais/paginado` | Mesmos filtros; `sort` ∈ `id`, `nome`, `especialidade`, `valorConsultaParticular` (padrão `nome`) | 200 `PageResponse` |
| `GET /me/profissionais/{id}` | Dados públicos de um profissional | 200 |
| `GET /me/profissionais/{id}/disponibilidade?data=2026-08-20` | Grade do dia | 200 |

`ProfissionalPublicoResponse` expõe apenas `id`, `nome`, `especialidade`,
`valorConsultaParticular` e `conveniosAceitos` (sem email, telefone ou registro).

A disponibilidade é uma grade fixa das **08:00 às 17:30, em intervalos de 30 minutos**
(20 horários), marcando como indisponível todo horário com consulta não cancelada:

```json
{"data": "2026-08-20", "horarios": [{"horario": "08:00:00", "disponivel": true}, {"horario": "08:30:00", "disponivel": false}]}
```

### Consultas

| Método e rota | Descrição | Resposta |
|---|---|---|
| `POST /me/consultas` | `{"profissionalId": 1, "dataHora": "...", "tipo": "PRESENCIAL", "convenio": null}` | 201 `ConsultaResponse`; 409 se ocupado |
| `GET /me/consultas` | Próprias consultas, mais recentes primeiro | 200 |
| `GET /me/consultas/{id}` | Detalhe | 200 |
| `PUT /me/consultas/{id}/cancelar` | Cancela | 204; 400 se já `REALIZADA` ou `CANCELADA` |
| `PUT /me/consultas/{id}/remarcar` | `{"novaDataHora": "..."}` | 200 `ConsultaResponse`; 400 se já `REALIZADA`/`CANCELADA`; 409 se ocupado |

Regras do agendamento pelo paciente:

- Sem `convenio` (nulo ou vazio) a consulta é particular e o `valor` é copiado de
  `valorConsultaParticular` do profissional; com convênio, `valor` fica nulo.
- Se o paciente ainda não tem profissional responsável (autocadastro), o profissional da
  primeira consulta passa a ser o responsável.
- O conflito de horário considera apenas o mesmo instante exato (`dataHora` igual) de uma
  consulta não cancelada do mesmo profissional.

### Mensagens

| Método e rota | Descrição | Resposta |
|---|---|---|
| `GET /me/mensagens/caixa-entrada` | Um item por profissional com quem há consulta, mesma ordenação da caixa do profissional | 200 |
| `GET /me/mensagens/{profissionalId}` | Conversa com o profissional | 200 `MensagemResponse[]` |
| `POST /me/mensagens/{profissionalId}` | `{"conteudo": "..."}` (até 2000); o autor é sempre `PACIENTE` | 201 |

### Avaliações

| Método e rota | Descrição | Resposta |
|---|---|---|
| `POST /me/avaliacoes` | `{"consultaId": 1, "nota": 5, "comentario": "..."}` | 201; mesmas regras da avaliação pelo profissional |
| `GET /me/avaliacoes/consulta/{consultaId}` | Avaliação da própria consulta | 200 |

### Prontuário

| Método e rota | Descrição | Resposta |
|---|---|---|
| `GET /me/prontuario/evolucoes` | Evoluções marcadas `visivelPaciente = true`, de todos os profissionais | 200 |
| `GET /me/prontuario/planos` | Planos marcados `visivelPaciente = true`, todas as revisões | 200 |

---

## Infraestrutura

| Rota | Descrição |
|---|---|
| `GET /actuator/health` | Estado geral |
| `GET /actuator/health/liveness`, `/actuator/health/readiness` | Probes para contêiner/orquestrador |
| `GET /actuator/info` | Nome da aplicação e `info.app.environment` |
| `GET /swagger-ui.html`, `GET /v3/api-docs` | Documentação OpenAPI |
| `/h2-console` | Console do H2, somente no profile `dev` |
