# Backlog — melhorias e pendências

Este documento reuniu as lacunas identificadas na revisão do código ao final do ciclo do
repositório original `fisiotech-back` (Etapas 1 a 4, chamadas de "Sprints 1 a 4" no `.docx`) e
serviu de insumo para a **Sprint 2 do board do `Fisio-Teach`**, que entregou as nove melhorias. O texto de cada melhoria
foi mantido como registro do planejamento; o quadro abaixo mostra o que foi de fato
implementado e onde a entrega diferiu do plano. As pendências que continuam em aberto estão
em [Pendências atuais](#pendências-atuais).

A numeração "Melhoria N" é local; a coluna *Issue* traz o número real no GitHub do `Fisio-Teach`.

## Situação após a Sprint 2

| Melhoria | Issue | Situação | Diferenças em relação ao plano |
|---|---|---|---|
| 1 — JWT | #5 | Entregue (`1928362`) | Chaves RSA (RS256) em arquivo PEM, em vez de um segredo simétrico; sessões revogáveis no banco. O interceptor do front fica no outro repositório |
| 2 — Flyway | #6 | Entregue (`dfc1faa`) | Sem `V2__dados_iniciais`; as migrações chegaram a `V5`. H2 e MySQL usam o mesmo conjunto |
| 3 — Homologação e APK | #8 | Parcial (`df74f93`) | Docker, Compose, profile `homolog` e CORS por variável prontos; URL pública e APK assinado dependem de infraestrutura externa |
| 4 — CI | #10 | Entregue (`26d5f89`) | Relatório de testes na PR incluído; proteção de branch e workflow do front não fazem parte deste repositório |
| 5 — Recuperação de senha | #11 | Entregue (`df407dd`) | Rotas `/auth/recuperar-senha` e `/auth/redefinir-senha`, com **código de 6 dígitos** em vez de token por link |
| 6 — Evolução e plano | #12 | Entregue (`6d9b571`) | Evolução com observações, procedimentos, resposta e conduta (sem escala de dor numérica); plano versionado por revisões, sem contagem de sessões. Rotas em `/prontuario/pacientes/{id}/...` e `/me/prontuario/...` |
| 7 — Agenda e indicadores | #13 | Entregue (`2ecde29`) | Rotas `/agenda` e `/agenda/indicadores` por período; sem média de avaliações nem contagem de remarcações |
| 8 — Paginação | #14 | Parcial (`f13ea15`) | Paginação em pacientes, pacientes (admin) e profissionais; consultas, mensagens e caixas de entrada continuam devolvendo listas completas |
| 9 — Testes de integração | #15 | Parcial (`7063a09`) | Autenticação, papéis, isolamento, cadastro, paginação, consultas e agenda cobertos via HTTP; mensagens, avaliações e prontuário ainda só nos testes de serviço |

---

## Melhoria 1 — Migrar a autenticação de HTTP Basic para JWT

**Situação atual:** `admin/config/SecurityConfig.java` usa `httpBasic(...)`. Toda requisição
carrega e-mail e senha em Base64, e o app precisa guardar a senha do usuário em claro para
repetir a chamada. Não há expiração de sessão nem logout real do lado do servidor.

**Escopo**
- `POST /auth/login` devolvendo access token (JWT assinado, ~15 min) e refresh token.
- `POST /auth/refresh` e `POST /auth/logout` (invalidação do refresh).
- Filtro de autenticação por `Authorization: Bearer`, mantendo os papéis
  `ROLE_ADMIN` / `ROLE_PROFISSIONAL` / `ROLE_PACIENTE` e as regras de rota já existentes.
- Segredo de assinatura vindo de variável de ambiente (nunca versionado).
- Front-end: interceptor que anexa o token, renova no 401 e desloga quando o refresh falha.

**Critério de aceite:** os fluxos do README (admin → profissional → paciente) passam a
funcionar apenas com token; nenhuma rota autenticada aceita mais Basic.

---

## Melhoria 2 — Versionar o schema do banco com migrações Flyway

**Situação atual:** `application-prod.properties` usa `ddl-auto=validate`, mas não existe
nenhum script de schema no repositório. O README instrui a "garantir que o schema MySQL já
exista", o que hoje significa gerar o banco na mão. Qualquer mudança de entidade quebra a
subida em produção sem aviso.

**Escopo**
- Adicionar `flyway-core` + `flyway-mysql` e `V1__schema_inicial.sql` refletindo as entidades
  atuais (admins, profissionais, pacientes, consultas, mensagens, avaliações e os campos
  `@Embedded` de quadro clínico, hábitos de vida, exame físico e diagnóstico).
- `V2__dados_iniciais.sql` opcional para carga de apoio em homologação.
- Manter H2 em dev com o mesmo conjunto de migrações, para dev e prod não divergirem.

**Critério de aceite:** subir a aplicação em um MySQL vazio com profile `prod` cria o schema
completo sem intervenção manual.

---

## Melhoria 3 — Publicar a API em ambiente de homologação e gerar o .apk conectado a ela

**Situação atual:** o profile `prod` existe mas nunca foi exercido; o `.apk` da Sprint 1
aponta para `localhost`. O `corsConfigurationSource` tem origens fixas
(`http://localhost`, `capacitor://localhost`, `http://localhost:4200`).

**Escopo**
- Publicar a API em um provedor gratuito/estudantil com MySQL gerenciado.
- Tornar as origens CORS configuráveis por variável de ambiente em vez de constantes no código.
- Build de produção do Angular apontando para a URL publicada e novo `.apk` assinado.
- Registrar no README a URL de homologação e as variáveis necessárias.

**Critério de aceite:** o `.apk` instalado em um celular qualquer, fora da rede de
desenvolvimento, completa o fluxo de cadastro → agendamento → mensagem.

**Dependência:** Melhoria 2 (schema versionado) precisa estar pronto antes.

---

## Melhoria 4 — Automatizar build e testes em pipeline de integração contínua

**Situação atual:** não existe `.github/workflows/`. Os testes de service escritos na Sprint 1
só rodam se alguém lembrar de executar `./mvnw test` localmente.

**Escopo**
- Workflow no GitHub Actions rodando `./mvnw -B verify` a cada push e pull request.
- Cache das dependências Maven e publicação do relatório de testes na PR.
- Branch protection exigindo o check verde antes do merge na `master`.
- Workflow equivalente para o repositório do front (build + lint).

**Critério de aceite:** uma PR com teste quebrado é bloqueada automaticamente.

---

## Melhoria 5 — Implementar a recuperação de senha por e-mail

**Situação atual:** existe apenas troca de senha autenticada (`PUT /me/senha` e o
autoatendimento de profissional/admin entregue em F-02), que exige a senha atual. Um usuário
que esquece a senha depende de intervenção manual no banco.

**Escopo**
- `POST /auth/senha/esqueci` gerando token de uso único com validade curta.
- `POST /auth/senha/redefinir` validando o token e trocando a senha.
- Envio de e-mail via `spring-boot-starter-mail` (SMTP por variável de ambiente).
- Resposta sempre genérica na solicitação, para não revelar quais e-mails existem na base.
- Telas correspondentes no app.

**Critério de aceite:** paciente, profissional e admin recuperam acesso sem suporte manual.

---

## Melhoria 6 — Registrar a evolução clínica e o plano terapêutico por sessão

**Situação atual:** `Consulta` guarda quadro clínico, hábitos de vida, exame físico e
diagnóstico — ou seja, a **avaliação inicial**. Não há como registrar o que foi feito em cada
sessão nem comparar a evolução do paciente ao longo do tratamento, que é justamente o núcleo
do acompanhamento fisioterapêutico.

**Escopo**
- Entidade `Evolucao` vinculada à consulta: condutas aplicadas, escala de dor (0–10),
  observações e data de registro.
- Plano terapêutico do paciente: objetivo, número de sessões previstas e sessões realizadas.
- `GET /pacientes/{id}/evolucoes` para o profissional e `GET /me/evolucoes` para o paciente.
- Histórico no app com a curva de dor ao longo das sessões.

**Critério de aceite:** o profissional registra a evolução ao concluir uma consulta e o
paciente visualiza seu histórico de tratamento.

---

## Melhoria 7 — Construir a agenda e os indicadores do profissional

**Situação atual:** existe `DisponibilidadeService` (horários livres para agendamento), mas o
profissional só tem uma listagem plana de consultas. Não há visão de dia/semana nem números
consolidados.

**Escopo**
- `GET /consultas/agenda?inicio=&fim=` agrupando as consultas do período por dia.
- `GET /profissionais/me/indicadores`: consultas do dia, próximas 7 dias, taxa de
  cancelamento, remarcações e média das avaliações recebidas.
- Tela inicial do profissional no app consumindo esses dois endpoints.

**Critério de aceite:** ao logar, o profissional vê a agenda do dia e os indicadores sem
precisar navegar por listas.

---

## Melhoria 8 — Paginar, filtrar e ordenar as listagens da API e do aplicativo

**Situação atual:** todos os repositórios retornam `List<T>` com `Sort`
(`PacienteRepository.findByProfissionalId`, `ConsultaRepository.findByProfissionalId`, a caixa
de entrada unificada). Sem paginação, cada tela carrega a base inteira — com poucos registros
de teste isso não aparece, mas degrada rápido em uso real e pesa no celular.

**Escopo**
- Trocar `List<T>` por `Page<T>` nas listagens de pacientes, consultas e caixa de entrada.
- Filtros por status e período em consultas; busca por nome em pacientes.
- Scroll infinito ou paginação nas telas correspondentes do app.
- Atualizar o Swagger e o README com os novos parâmetros.

**Critério de aceite:** nenhuma listagem devolve mais que uma página por requisição.

---

## Melhoria 9 — Cobrir os endpoints com testes de integração automatizados

**Situação atual:** há testes de unidade para os services, com repositórios mockados. A regra
de isolamento por dono — devolver **404 em vez de 403** quando o recurso é de outro
profissional — e a regra de mensagens ("só existe conversa se existir consulta entre as
partes") não são verificadas na camada HTTP, que é onde elas realmente valem.

**Escopo**
- Testes `@SpringBootTest` + `MockMvc` sobre H2 cobrindo os fluxos do README de ponta a ponta.
- Casos de autorização negativa: cada papel tentando acessar rotas dos outros dois.
- Casos de regra de negócio: horário ocupado, avaliação antes da consulta realizada, mensagem
  sem consulta prévia.
- Pipeline (Melhoria 4) executando essa suíte.

**Critério de aceite:** a suíte falha se alguém remover uma verificação de posse de recurso.

---

## Dependências entre as melhorias

```
Melhoria 2 (Flyway) ──► Melhoria 3 (homologação)
Melhoria 1 (JWT)    ──► Melhoria 3
Melhoria 4 (CI)     ──► Melhoria 9 (a suíte precisa rodar no pipeline)
Melhorias 5, 6, 7 e 8 são independentes entre si
```

Sugestão de ordem: 2 e 1 primeiro, porque destravam a 3; a 4 em paralelo, por ser isolada;
e as melhorias de produto (5, 6, 7 e 8) distribuídas entre os demais integrantes.

---

## Pendências atuais

Levantadas na revisão do código após a Sprint 2. Nenhuma delas tem issue aberta ainda.

| # | Pendência | Onde | Sugestão |
|---|---|---|---|
| P1 | Chaves JWT de exemplo da homologação usam `classpath:keys/*.pem`, que nunca entram na imagem (`*.pem` ignorado pelo Git e pelo Docker) | `.env.homolog.example` | Trocar por `file:/run/keys/*.pem` e declarar o volume em `compose.homolog.yml` |
| P2 | O profissional define o campo `autor` em `POST /mensagens` e pode gravar mensagem como `PACIENTE` | `MensagemCreateRequest`, `MensagemService.enviar` | Remover `autor` do request e fixar `PROFISSIONAL`, como já é feito em `/me/mensagens` |
| P3 | Agendamento aceita horário fora da grade de 30 min e datas no passado | `ConsultaService.validarHorarioLivre` | Validar `dataHora` futura e alinhada à grade de `DisponibilidadeService` |
| P4 | Grade de disponibilidade fixa (08:00–17:30, todos os dias) | `DisponibilidadeService` | Expediente configurável por profissional, com dias de folga |
| P5 | Listagens sem paginação: consultas, agenda, mensagens e caixas de entrada | controllers de `consulta`, `mensagem`, `me` | Completar a Melhoria 8 |
| P6 | Mensagens, avaliações e prontuário sem testes HTTP | `src/test` | Completar a Melhoria 9 |
| P7 | Erro de validação não informa o campo inválido | `ApiExceptionHandler.handleValidation` | Incluir a lista de campos e mensagens dos DTOs |
| P8 | Sem limpeza de sessões, refresh tokens e códigos de recuperação expirados | `auth_*`, `codigos_recuperacao_senha` | Tarefa agendada (`@Scheduled`) respeitando a ordem descrita em [Autenticação JWT](autenticacao-jwt.md#banco-de-produção-e-implantação) |
| P9 | Sem limite de frequência em `/auth/login` e `/auth/recuperar-senha` | `AuthController` | Rate limiting por IP/email na aplicação ou no proxy |
| P10 | Nomes de variáveis diferentes entre `homolog` e `prod` (`DB_USER`/`DB_USERNAME`, `ADMIN_PASSWORD`/`ADMIN_SENHA`, `MAIL_*`/`SPRING_MAIL_*`) | `application-homolog.properties`, `application-prod.properties` | Unificar os nomes |
| P11 | Arquivos sem uso no repositório: `arquivo.txt` e `src/main/resources/TODO` (vazio) | raiz e `resources` | Remover, se a equipe confirmar |
