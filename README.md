# FisioTech — Backend

[![CI](https://github.com/LarissaFelixx/Fisio-Teach/actions/workflows/ci.yml/badge.svg)](https://github.com/LarissaFelixx/Fisio-Teach/actions/workflows/ci.yml)

API REST de um sistema de gestão de clínica de fisioterapia. Um **administrador** cadastra
**profissionais**; cada profissional gerencia seus **pacientes**, **consultas** (com registro
clínico), **prontuário** (evoluções e plano terapêutico), **agenda com indicadores**,
**mensagens** e **avaliações**; e o **paciente** se cadastra, busca profissionais, agenda,
remarca ou cancela consultas, conversa com o profissional e acompanha o próprio tratamento
pela área de autoatendimento `/me`.

O cliente é um aplicativo Angular + Capacitor mantido em outro repositório:
[LarissaFelixx/FisioTech-front](https://github.com/LarissaFelixx/FisioTech-front)
(originalmente [gabrielneriqa/fisiotech-front](https://github.com/gabrielneriqa/fisiotech-front)).

## Sumário

- [Stack](#stack)
- [Início rápido](#início-rápido)
- [Profiles e configuração](#profiles-e-configuração)
- [Autenticação e papéis](#autenticação-e-papéis)
- [Funcionalidades](#funcionalidades)
- [Testando a API do zero](#testando-a-api-do-zero-fluxo-completo-via-curl)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Testes e integração contínua](#testes-e-integração-contínua)
- [Homologação e produção](#homologação-e-produção)
- [Limitações conhecidas](#limitações-conhecidas)
- [Solução de problemas](#solução-de-problemas-comuns)
- [Mapa da documentação](#mapa-da-documentação)

## Stack

| Tecnologia | Uso |
|---|---|
| Java 21 | Linguagem (o build também roda em JDKs mais novos) |
| Spring Boot 4.0.5 | Web MVC, Data JPA, Security, OAuth2 Resource Server (JWT), Validation, Mail, Actuator |
| Flyway | Migrações versionadas do schema (H2 e MySQL) |
| H2 | Banco em memória no profile `dev` e nos testes |
| MySQL 8 | Banco de homologação/produção (`mysql-connector-j`) |
| springdoc-openapi 3 | Swagger UI e especificação OpenAPI |
| Lombok | Redução de código repetitivo nas entidades |
| Maven Wrapper | Build sem instalar Maven (`mvnw` / `mvnw.cmd`) |
| Docker + Compose | Imagem e ambiente de homologação |
| GitHub Actions | Build, testes e relatório a cada push/PR |

## Início rápido

Pré-requisitos: **JDK 21** (ou mais novo) no `PATH` ou em `JAVA_HOME`, e Git. Maven e banco de
dados **não** são necessários para rodar em `dev`.

```bash
git clone https://github.com/LarissaFelixx/Fisio-Teach.git
cd Fisio-Teach
```

Suba em modo `dev` (H2 em memória + admin pronto):

```bash
# Linux/macOS/Git Bash
./dev.sh          # porta 8080
./dev.sh 8081     # outra porta

# Windows PowerShell
.\dev.ps1
.\dev.ps1 -Port 8081
```

Os scripts equivalem a:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev -Dspring-boot.run.arguments=--server.port=8080
```

Com a aplicação no ar:

| O quê | Onde |
|---|---|
| API | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| Especificação OpenAPI | `http://localhost:8080/v3/api-docs` |
| Saúde | `http://localhost:8080/actuator/health` |
| Console H2 | `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:...` impressa no log, usuário `sa`, sem senha) |
| Admin | `admin@fisiotech.com` / `12345678` |

O banco é **em memória**: todo dado se perde ao reiniciar, e os JWTs emitidos deixam de valer
(o par de chaves RSA também é recriado a cada subida).

No Swagger, faça `POST /auth/login`, copie só o `accessToken` e cole em **Authorize**.

Se mudar a porta, ajuste também o `proxy.conf.js` do front-end.

## Profiles e configuração

| Profile | Para quê | Banco | Como ativar |
|---|---|---|---|
| `dev` | Desenvolvimento local | H2 em memória | `dev.sh`/`dev.ps1` ou `-Dspring-boot.run.profiles=dev` |
| `homolog` | Contêiner de homologação | MySQL 8 | Fixado no `ENTRYPOINT` do `Dockerfile` |
| `prod` | Produção | MySQL 8 | `SPRING_PROFILES_ACTIVE=prod` |

Sem nenhum profile a aplicação **não sobe**: faltam as propriedades do admin e das chaves JWT.

Em todos os profiles o Flyway aplica as migrações pendentes na subida e o Hibernate apenas
valida o schema (`ddl-auto=validate`).

### Variáveis de ambiente

Os nomes **diferem** entre `homolog` e `prod` — confira a coluna certa.

| Finalidade | `homolog` | `prod` | Obrigatória |
|---|---|---|---|
| URL JDBC do MySQL | `DB_URL` (padrão `jdbc:mysql://mysql:3306/fisiotech?createDatabaseIfNotExist=true&serverTimezone=UTC`) | `DB_URL` | prod |
| Usuário do banco | `DB_USER` (padrão `fisiotech`) | `DB_USERNAME` | prod |
| Senha do banco | `DB_PASSWORD` | `DB_PASSWORD` | sim |
| Senha root do MySQL (só Compose) | `DB_ROOT_PASSWORD` | — | homolog |
| Nome do admin | `ADMIN_NAME` (padrão `Administrador`) | `ADMIN_NOME` | prod |
| Email do admin | `ADMIN_EMAIL` | `ADMIN_EMAIL` | sim |
| Senha do admin (mín. 8) | `ADMIN_PASSWORD` | `ADMIN_SENHA` | sim |
| Chave pública JWT (PEM X.509) | `JWT_PUBLIC_KEY` | `JWT_PUBLIC_KEY` | sim |
| Chave privada JWT (PEM PKCS#8) | `JWT_PRIVATE_KEY` | `JWT_PRIVATE_KEY` | sim |
| Servidor SMTP | `MAIL_HOST` | `SPRING_MAIL_HOST` | homolog² / prod não¹ |
| Porta SMTP | `MAIL_PORT` (padrão `587`) | `SPRING_MAIL_PORT` | não |
| Usuário SMTP | `MAIL_USERNAME` | `SPRING_MAIL_USERNAME` | homolog² |
| Senha SMTP | `MAIL_PASSWORD` | `SPRING_MAIL_PASSWORD` | homolog² |
| Remetente dos emails | `MAIL_FROM` | `MAIL_REMETENTE` | prod |
| Autenticação/STARTTLS SMTP | `MAIL_SMTP_AUTH`, `MAIL_STARTTLS` (padrão `true`) | fixos em `true` | não |
| Origens CORS (separadas por vírgula) | `CORS_ALLOWED_ORIGINS` | `CORS_ALLOWED_ORIGINS` (opcional) | homolog |
| Nome do ambiente em `/actuator/info` | `APP_ENVIRONMENT` | `APP_ENVIRONMENT` | não |
| Porta publicada pelo Compose | `API_PORT` (padrão `8080`) | — | não |

¹ Sem SMTP a aplicação sobe, mas o código de recuperação de senha não é enviado (apenas um
erro no log).

² Em `application-homolog.properties` essas variáveis são referenciadas sem valor padrão:
declare-as no `.env.homolog`, ainda que vazias (como no `.env.homolog.example`).

As chaves JWT são **URIs de recurso Spring**, como `file:/etc/fisiotech/jwt-private.pem`. O
par precisa ter pelo menos 2048 bits; a aplicação não sobe se as chaves faltarem ou não
formarem um par. Como gerar: [Autenticação JWT](docs/autenticacao-jwt.md#configuração).

Propriedades com padrão em `application.properties`, que podem ser sobrescritas:

| Propriedade | Padrão |
|---|---|
| `app.jwt.issuer` / `app.jwt.audience` | `fisiotech` / `fisiotech-api` |
| `app.jwt.access-ttl` | `15m` |
| `app.jwt.refresh-ttl` (duração máxima da sessão) | `7d` |
| `app.recuperacao-senha.validade-minutos` | `15` |

## Autenticação e papéis

A API usa **JWT Bearer** com sessões revogáveis. Faça `POST /auth/login` com email e senha e
envie `Authorization: Bearer <accessToken>`. O access token dura 15 minutos e é renovado com
`POST /auth/refresh`; `POST /auth/logout` encerra a sessão. HTTP Basic não é aceito. Fluxo
completo, regras de rotação e orientações para o app: [Autenticação JWT](docs/autenticacao-jwt.md).

| Papel | Rotas | Como a conta nasce |
|---|---|---|
| `ROLE_ADMIN` | `/profissionais/**`, `/admin/pacientes/**`, `/admin/me/**` | Único; criado na primeira subida (fixo no `dev`, variáveis em `homolog`/`prod`) |
| `ROLE_PROFISSIONAL` | `/pacientes/**`, `/consultas/**`, `/agenda/**`, `/prontuario/**`, `/mensagens/**`, `/avaliacoes/**`, `/profissionais/me/**` | Cadastrado pelo admin |
| `ROLE_PACIENTE` | `/me/**` | Cadastrado por um profissional ou por autocadastro público (`POST /pacientes/cadastro`) |

Regras que valem para toda a API:

- **Isolamento por dono**: profissional e paciente só veem os próprios recursos. Pedir o
  recurso de outra pessoa retorna **404**, não 403, para não confirmar que ele existe.
- **Mensagens dependem de consulta**: só há conversa entre paciente e profissional que tenham
  pelo menos uma consulta entre si (qualquer status, inclusive cancelada).
- **Email único global**: o mesmo email não pode ser usado por dois tipos de conta.
- **Troca de senha derruba as sessões**: trocar, redefinir ou ter a senha alterada pelo
  admin encerra todos os tokens da conta na hora.

### Recuperação de senha ("esqueci minha senha")

Vale para os três papéis, em duas etapas, ambas **sem autenticação**:

1. `POST /auth/recuperar-senha` com `{"email": "..."}` — gera um código de **6 dígitos**,
   válido por **15 minutos**, e envia por email. Responde **sempre 204**, exista ou não a conta.
   Pedir de novo invalida o código anterior.
2. `POST /auth/redefinir-senha` com `{"email": "...", "codigo": "123456", "novaSenha": "..."}` —
   troca a senha, encerra todas as sessões da conta e responde 204. Código errado, expirado,
   já usado ou após **5 tentativas** erradas: **400** `"Código inválido ou expirado."`.

No profile `dev` nenhum email é enviado: o código aparece no log, numa linha
`[DEV] Código de recuperação de senha para ...`.

```bash
curl -X POST http://localhost:8080/auth/recuperar-senha \
  -H "Content-Type: application/json" \
  -d '{"email": "joao@paciente.com"}'

# pegue o código no log e:
curl -X POST http://localhost:8080/auth/redefinir-senha \
  -H "Content-Type: application/json" \
  -d '{"email": "joao@paciente.com", "codigo": "123456", "novaSenha": "outraSenha123"}'
```

## Funcionalidades

| Área | O que existe | Rotas principais |
|---|---|---|
| Profissionais | CRUD pelo admin, busca pública por nome/especialidade, troca de senha própria | `/profissionais`, `/me/profissionais`, `/profissionais/me/senha` |
| Pacientes | Cadastro pelo profissional, autocadastro, gestão global pelo admin (inclusive troca de responsável), perfil próprio | `/pacientes`, `/pacientes/cadastro`, `/admin/pacientes`, `/me` |
| Consultas | Agendamento pelo profissional ou paciente, bloqueio de horário duplicado, cancelamento, remarcação, registro clínico (quadro clínico, hábitos, exame físico, diagnóstico) | `/consultas`, `/me/consultas` |
| Disponibilidade | Grade de 08:00 a 17:30 em intervalos de 30 min, marcando horários ocupados | `/me/profissionais/{id}/disponibilidade` |
| Agenda e indicadores | Consultas por período com filtros; total, pacientes atendidos, taxa de cancelamento e distribuição por status | `/agenda`, `/agenda/indicadores` |
| Prontuário | Evoluções clínicas imutáveis e plano terapêutico versionado, com controle do que o paciente vê | `/prontuario/pacientes/{id}/evolucoes`, `/prontuario/pacientes/{id}/planos`, `/me/prontuario/evolucoes`, `/me/prontuario/planos` |
| Mensagens | Conversa por par paciente–profissional e caixa de entrada unificada para ambos | `/mensagens`, `/me/mensagens` |
| Avaliações | Nota de 1 a 5 e comentário, só para consulta realizada, uma por consulta | `/avaliacoes`, `/me/avaliacoes` |
| Paginação | `page`, `size` (máx. 100), `sort` (lista permitida por rota) e `direction` | `/pacientes/paginado`, `/admin/pacientes/paginado`, `/profissionais/paginado`, `/me/profissionais/paginado` |
| Autenticação | Login, refresh com rotação, logout, `/auth/me`, recuperação de senha por código | `/auth/**` |

Todos os endpoints, corpos, validações e códigos de resposta: [Referência da API](docs/api-referencia.md).
Regras de negócio e modelo de dados: [Arquitetura](docs/arquitetura.md).

## Testando a API do zero (fluxo completo via curl)

Com a aplicação em `dev` (`http://localhost:8080`), este roteiro cria os dados e percorre os
três papéis: autocadastro de paciente, busca e agendamento, prontuário e troca de mensagens.
Ele foi executado de ponta a ponta contra a API nesta versão.

Requisitos: Bash (no Windows, o Git Bash), curl e **[jq](https://jqlang.org/download/)** — que
não vem com o Git Bash; no Windows instale com `winget install jqlang.jq`.

> **Acentos no Windows.** No Git Bash/PowerShell, texto acentuado passado direto em `-d '...'`
> pode sair fora de UTF-8, e a API responde 400 (`Invalid UTF-8`). Os exemplos abaixo evitam
> acentos; para enviar texto acentuado, grave o JSON num arquivo UTF-8 e use
> `--data-binary @arquivo.json`.

Token do administrador:

```bash
ADMIN_TOKEN=$(curl -fsS http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@fisiotech.com","senha":"12345678"}' | jq -r .accessToken)
```

**1. Admin cria um profissional:**

```bash
curl -H "Authorization: Bearer $ADMIN_TOKEN" -X POST http://localhost:8080/profissionais \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Ana Souza",
    "email": "ana@fisiotech.com",
    "senha": "senha123",
    "registroProfissional": "CREFITO-11111",
    "especialidade": "Ortopedia",
    "valorConsultaParticular": 150.00,
    "conveniosAceitos": ["Unimed", "Amil"]
  }'
```

**2. Profissional faz login e confere a identidade:**

```bash
PROFISSIONAL_TOKEN=$(curl -fsS http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"ana@fisiotech.com","senha":"senha123"}' | jq -r .accessToken)

curl -H "Authorization: Bearer $PROFISSIONAL_TOKEN" http://localhost:8080/auth/me
```

**3. Paciente se autocadastra (rota pública; ainda sem profissional vinculado) e faz login:**

```bash
curl -X POST http://localhost:8080/pacientes/cadastro \
  -H "Content-Type: application/json" \
  -d '{"nome": "Joao Silva", "email": "joao@paciente.com", "senha": "senha123"}'

PACIENTE_TOKEN=$(curl -fsS http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"joao@paciente.com","senha":"senha123"}' | jq -r .accessToken)
```

**4. Paciente busca profissionais e vê os horários livres (o `id` da Ana é normalmente `1`):**

```bash
curl -H "Authorization: Bearer $PACIENTE_TOKEN" "http://localhost:8080/me/profissionais?especialidade=Ortopedia"
curl -H "Authorization: Bearer $PACIENTE_TOKEN" "http://localhost:8080/me/profissionais/1/disponibilidade?data=2026-08-20"
```

**5. Paciente marca a consulta — a primeira consulta vincula a Ana como responsável:**

```bash
curl -H "Authorization: Bearer $PACIENTE_TOKEN" -X POST http://localhost:8080/me/consultas \
  -H "Content-Type: application/json" \
  -d '{"profissionalId": 1, "dataHora": "2026-08-20T09:00:00", "tipo": "PRESENCIAL", "convenio": null}'
```

**6. Profissional vê o paciente na lista e envia uma mensagem (permitida porque já existe consulta):**

```bash
curl -H "Authorization: Bearer $PROFISSIONAL_TOKEN" http://localhost:8080/pacientes
curl -H "Authorization: Bearer $PROFISSIONAL_TOKEN" -X POST http://localhost:8080/mensagens \
  -H "Content-Type: application/json" \
  -d '{"pacienteId": 1, "autor": "PROFISSIONAL", "conteudo": "Oi Joao, como vai o tratamento?"}'
```

**7. Paciente lê e responde:**

```bash
curl -H "Authorization: Bearer $PACIENTE_TOKEN" http://localhost:8080/me/mensagens/caixa-entrada
curl -H "Authorization: Bearer $PACIENTE_TOKEN" http://localhost:8080/me/mensagens/1
curl -H "Authorization: Bearer $PACIENTE_TOKEN" -X POST http://localhost:8080/me/mensagens/1 \
  -H "Content-Type: application/json" \
  -d '{"conteudo": "Oi Ana, tudo certo!"}'
```

**8. Profissional confere a conversa e a caixa de entrada:**

```bash
curl -H "Authorization: Bearer $PROFISSIONAL_TOKEN" "http://localhost:8080/mensagens?pacienteId=1"
curl -H "Authorization: Bearer $PROFISSIONAL_TOKEN" http://localhost:8080/mensagens/caixa-entrada
```

**9. Profissional registra o plano terapêutico e uma evolução visíveis ao paciente:**

```bash
curl -H "Authorization: Bearer $PROFISSIONAL_TOKEN" -X POST http://localhost:8080/prontuario/pacientes/1/planos \
  -H "Content-Type: application/json" \
  -d '{"objetivos": "Reduzir dor lombar", "condutas": "Fortalecimento de core 2x/semana",
       "dataInicio": "2026-08-20", "dataFimPrevista": "2026-10-20", "visivelPaciente": true}'

curl -H "Authorization: Bearer $PROFISSIONAL_TOKEN" -X POST http://localhost:8080/prontuario/pacientes/1/evolucoes \
  -H "Content-Type: application/json" \
  -d '{"consultaId": 1, "observacoes": "Dor 6/10, boa resposta ao alongamento", "visivelPaciente": true}'

curl -H "Authorization: Bearer $PACIENTE_TOKEN" http://localhost:8080/me/prontuario/planos
curl -H "Authorization: Bearer $PACIENTE_TOKEN" http://localhost:8080/me/prontuario/evolucoes
```

**10. Agenda e indicadores do profissional:**

```bash
curl -H "Authorization: Bearer $PROFISSIONAL_TOKEN" \
  "http://localhost:8080/agenda?inicio=2026-08-01T00:00:00&fim=2026-08-31T23:59:59"
curl -H "Authorization: Bearer $PROFISSIONAL_TOKEN" \
  "http://localhost:8080/agenda/indicadores?inicio=2026-08-01T00:00:00&fim=2026-08-31T23:59:59"
```

**11. Paciente remarca a consulta e troca a senha (a troca encerra a sessão; faça login de novo depois):**

```bash
curl -H "Authorization: Bearer $PACIENTE_TOKEN" -X PUT http://localhost:8080/me/consultas/1/remarcar \
  -H "Content-Type: application/json" \
  -d '{"novaDataHora": "2026-08-21T10:00:00"}'

curl -H "Authorization: Bearer $PACIENTE_TOKEN" -X PUT http://localhost:8080/me/senha \
  -H "Content-Type: application/json" \
  -d '{"senhaAtual": "senha123", "novaSenha": "novaSenha123"}'
```

Observações:

- `PUT /me/consultas/{id}/cancelar` cancela em vez de remarcar.
- Para avaliar, o profissional marca a consulta como `REALIZADA` (`PUT /consultas/{id}`) e o
  paciente envia `POST /me/avaliacoes`.
- `POST /pacientes` (sem `/cadastro`) cria um paciente já vinculado ao profissional logado. Mesmo
  assim, a conversa só fica disponível depois de existir uma consulta entre os dois.

## Estrutura do projeto

```
.
├── .github/workflows/ci.yml        # pipeline de build e testes
├── Dockerfile                      # imagem da API (profile homolog)
├── compose.homolog.yml             # API + MySQL para homologação
├── .env.homolog.example            # modelo das variáveis de homologação
├── dev.sh / dev.ps1                # sobem a API em modo dev
├── docs/                           # documentação (ver Mapa da documentação)
└── src/
    ├── main/java/com/app/fisiotech/
    │   ├── admin/          # Admin, SecurityConfig, AdminInitializer, /admin/me
    │   ├── auth/           # login/refresh/logout, JWT, sessões, recuperação de senha, /auth/me
    │   ├── profissional/   # Profissional
    │   ├── paciente/       # Paciente (profissional, admin e autocadastro)
    │   ├── consulta/       # Consulta + registro clínico, disponibilidade, agenda e indicadores
    │   ├── prontuario/     # evoluções clínicas e planos terapêuticos
    │   ├── mensagem/       # mensagens e caixa de entrada
    │   ├── avaliacao/      # avaliações de consultas
    │   ├── me/             # autoatendimento do paciente (/me)
    │   ├── common/         # paginação (PageResponse, PageRequestFactory)
    │   └── exception/      # exceções de negócio e ApiExceptionHandler
    ├── main/resources/
    │   ├── application*.properties   # base, dev, homolog, prod
    │   ├── db/migration/             # Flyway V1–V5
    │   └── db/manual/                # scripts SQL de apoio à migração JWT de bases legadas
    └── test/                         # testes de unidade, integração HTTP e migração
```

Cada domínio segue `controller` → `service` → `repository` → `entity`, com `dto` para entrada e
saída. Detalhes em [Arquitetura](docs/arquitetura.md).

## Testes e integração contínua

```bash
./mvnw test        # Linux/macOS/Git Bash
mvnw.cmd test      # Windows
```

A suíte tem **109 testes em 20 classes**: unidade dos services, integração HTTP com servidor real
e H2 (autenticação, papéis, isolamento, revogação, recuperação de senha, cadastro, paginação,
consulta, agenda e indicadores) e validação das migrações. O teste de migração em MySQL só roda
quando `MYSQL_TEST_URL` está definida (ele também lê `MYSQL_TEST_USERNAME` e
`MYSQL_TEST_PASSWORD`); sem ela, aparece como ignorado. Cobertura por área: [Matriz de testes](docs/matriz-testes.md).

Os nomes dos métodos de teste viram frases nos relatórios (`deveRecusarCodigoExpirado` →
"Deve recusar codigo expirado"), via `FraseDisplayNameGenerator` registrado em
`src/test/resources/junit-platform.properties`. Escreva os nomes descrevendo o comportamento
esperado, em camelCase; um `@DisplayName` explícito tem prioridade.

**CI**: todo push e pull request para `main` ou `dev-marcoNoronha` (e execução manual) dispara
[`.github/workflows/ci.yml`](.github/workflows/ci.yml), que:

1. Sobe um MySQL 8.4 de serviço e instala o JDK 21 (Temurin) com cache do Maven.
2. Roda `./mvnw verify`: compila, executa os testes (inclusive as migrações no MySQL) e
   empacota o jar.
3. Publica o **Relatório de testes** (resumo da execução, check no commit com anotação na
   linha das falhas e, em PRs, um comentário atualizado a cada push).
4. Anexa os XMLs do Surefire (`test-reports`, 7 dias) e o jar (`fisiotech-backend`).

Um push novo na mesma branch cancela a execução anterior.

## Homologação e produção

- **Homologação em contêiner**: `docker compose -f compose.homolog.yml up --build -d` sobe a API
  (profile `homolog`) e um MySQL 8.4 com volume persistente. Passo a passo, chaves e integração do
  APK: [Homologação e APK](docs/homologacao-apk.md).
- **Produção**: gere o jar com `./mvnw package`, defina as variáveis da coluna `prod` e rode
  `java -jar target/fisiotech-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod`.
- **Bases MySQL criadas antes do Flyway/JWT**: siga a seção
  [Banco de produção e implantação](docs/autenticacao-jwt.md#banco-de-produção-e-implantação)
  antes da primeira subida.
- Verifique `GET /actuator/health/readiness` = `UP` após a implantação.

## Limitações conhecidas

Pontos identificados na revisão do código e ainda não tratados. Estão também no
[Backlog](docs/backlog-futuro.md#pendências-atuais).

| Ponto | Impacto |
|---|---|
| `.env.homolog.example` aponta `JWT_*_KEY` para `classpath:keys/*.pem`, mas `*.pem` é ignorado pelo Git e pelo Docker | A imagem de homologação não sobe com esses valores; use `file:` e monte as chaves como volume (ver [Homologação](docs/homologacao-apk.md)) |
| `POST /mensagens` aceita o campo `autor` enviado pelo profissional | O profissional consegue gravar uma mensagem com autor `PACIENTE` |
| O agendamento não confere se `dataHora` está na grade de 30 min nem se está no passado | É possível marcar 09:10 ou datas passadas; o conflito só é detectado para o mesmo instante exato |
| A grade de disponibilidade é fixa (08:00–17:30, todos os dias) | Não há cadastro de expediente, folgas ou fins de semana por profissional |
| Listagens sem paginação (`/consultas`, `/me/consultas`, mensagens, caixas de entrada, `/agenda`) | Carregam tudo de uma vez |
| Erro de validação retorna só `"Dados da requisição inválidos."` | O cliente não sabe qual campo falhou |
| Não há limpeza agendada de sessões, refresh tokens e códigos de recuperação expirados | As tabelas crescem indefinidamente |
| Sem limite de frequência em login e `/auth/recuperar-senha` | Depende de proteção na infraestrutura (proxy/WAF) |

## Solução de problemas comuns

- **Erro de certificado TLS ao baixar dependências** (`PKIX path building failed`) — comum em
  redes com inspeção de TLS. No Windows:
  ```powershell
  $env:MAVEN_OPTS = "-Djavax.net.ssl.trustStoreType=Windows-ROOT"
  ```
- **Porta 8080 ocupada** (no Windows, às vezes pelo NVIDIA Broadcast) — use `./dev.sh 8081` ou
  `.\dev.ps1 -Port 8081`.
- **`JAVA_HOME` não definido** — aponte para o JDK, ex.
  `$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"`.
- **Aplicação não sobe sem profile** — ative `dev` localmente; os outros profiles exigem as
  variáveis da tabela acima.
- **401 depois de reiniciar em `dev`** — normal: as chaves JWT são recriadas a cada subida.
  Faça login de novo.
- **Falha na subida com email duplicado** — o `EmailRegistryInitializer` encontrou o mesmo
  email em dois tipos de conta. Rode `src/main/resources/db/manual/000-audit-emails.sql` e
  resolva os conflitos.

## Mapa da documentação

| Documento | Conteúdo | Público |
|---|---|---|
| [Referência da API](docs/api-referencia.md) | Todos os endpoints, corpos, validações, erros e paginação | Desenvolvimento |
| [Arquitetura](docs/arquitetura.md) | Pacotes, modelo de dados, segurança e regras de negócio | Desenvolvimento |
| [Autenticação JWT](docs/autenticacao-jwt.md) | Contrato de tokens, sessões, app mobile, chaves e migração de banco legado | Desenvolvimento / implantação |
| [Homologação e APK](docs/homologacao-apk.md) | Subir a homologação com Docker e gerar o APK apontando para ela | Implantação |
| [Matriz de testes](docs/matriz-testes.md) | Cobertura automatizada por área | Desenvolvimento / avaliação |
| [Board de sprints](docs/board-sprints.md) | Issues do board com o commit de evidência e histórico do `fisiotech-back` | Avaliação acadêmica |
| [Entrega da Sprint 2](docs/entrega-sprint.md) | Escopo (issues #5–#16), commits e critérios de validação da Sprint 2 | Avaliação acadêmica |
| [Backlog](docs/backlog-futuro.md) | Melhorias planejadas (entregues na Sprint 2) e pendências atuais | Planejamento |
| `docs/FisioTech_Documentacao_Entrega_Sprints.docx` / `.pdf` | Documento formal do ciclo do `fisiotech-back` ("Sprints 1 a 4" = Etapas 1 a 4 do [board](docs/board-sprints.md#histórico-do-repositório-original-fisiotech-back)), gerado por `docs/gerar_documento_sprints.py` | Avaliação acadêmica (registro histórico; não cobre a Sprint 2 do board) |
