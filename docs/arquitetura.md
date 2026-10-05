# Arquitetura e regras de negócio

Visão de como o backend está organizado, como os dados são guardados e quais regras de
negócio estão implementadas. Para a lista de endpoints, veja a
[Referência da API](api-referencia.md); para o fluxo de tokens, [Autenticação JWT](autenticacao-jwt.md).

## Visão geral

```
App mobile (Angular + Capacitor)  ──HTTPS + Bearer JWT──►  API Spring Boot  ──JPA/Flyway──►  MySQL 8 (homolog/prod)
Dev server Angular (:4200)                                   │                               H2 em memória (dev/testes)
                                                             └── SMTP (código de recuperação de senha)
```

- **Aplicação stateless**: não há sessão HTTP nem cookie. Cada requisição traz o JWT, e o
  servidor confere no banco se a sessão daquele token continua válida.
- **Monólito modular por domínio**: cada pasta de `src/main/java/com/app/fisiotech` é um
  domínio com suas próprias camadas.

## Pacotes

| Pacote | Responsabilidade |
|---|---|
| `admin/` | Entidade `Admin`, troca de senha do admin (`/admin/me`), `SecurityConfig` (regras de rota, CORS, BCrypt, respostas 401/403) e `AdminInitializer` (cria o admin na primeira subida) |
| `auth/` | Login, refresh, logout e `/auth/me`; recuperação de senha por código; emissão e validação de JWT (`JwtConfig`); sessões (`auth_sessions`), refresh tokens e registro global de emails (`auth_emails`); `AppUserDetailsService` e `AuthenticatedUser`; configuração do Swagger |
| `profissional/` | Cadastro e manutenção de profissionais (pelo admin), busca pública e troca de senha própria |
| `paciente/` | Pacientes do profissional, visão administrativa (`/admin/pacientes`) e autocadastro público |
| `consulta/` | Consultas com registro clínico embutido, disponibilidade de horários e agenda/indicadores |
| `mensagem/` | Conversa paciente ↔ profissional e caixas de entrada |
| `avaliacao/` | Nota de 1 a 5 sobre uma consulta realizada |
| `prontuario/` | Evoluções clínicas e planos terapêuticos versionados |
| `me/` | `MeController`: toda a área de autoatendimento do paciente (`/me/**`), reaproveitando os services dos outros domínios |
| `common/` | `PageResponse` e `PageRequestFactory` (paginação padronizada) |
| `exception/` | Exceções de negócio e `ApiExceptionHandler` (tradução para status HTTP) |

Dentro de cada domínio: `controller` → `service` → `repository` → `entity`, com `dto` (records
de entrada/saída). Os controllers não acessam repositórios diretamente; regras de posse e de
estado ficam nos services.

## Modelo de dados

O schema é versionado pelo Flyway em `src/main/resources/db/migration` e o Hibernate apenas
valida (`ddl-auto=validate`) em todos os profiles, inclusive `dev`.

| Migração | Conteúdo |
|---|---|
| `V1__estrutura_inicial.sql` | `admins`, `profissionais`, `profissional_convenios`, `pacientes`, `consultas`, `mensagens`, `avaliacoes` |
| `V2__seguranca_jwt.sql` | `auth_emails`, `auth_sessions`, `auth_refresh_tokens` e carga de `auth_emails` com as contas existentes |
| `V3__recuperacao_senha.sql` | `password_reset_tokens` (primeira versão, por link — removida na V5) |
| `V4__prontuario_clinico.sql` | `evolucoes_clinicas`, `planos_terapeuticos` |
| `V5__recuperacao_senha_por_codigo.sql` | Remove `password_reset_tokens` e cria `codigos_recuperacao_senha` |

```
admins
profissionais ──< profissional_convenios
      │
      ├──< pacientes (profissional_id, opcional: nulo no autocadastro)
      │         │
      ├─────────┴──< consultas >── avaliacoes (1:1, consulta_id único)
      │         │         │
      ├─────────┴──< mensagens
      │         │         │
      ├─────────┴──< evolucoes_clinicas (consulta_id opcional)
      └─────────┴──< planos_terapeuticos

auth_emails (subject "TIPO:id" → email único global)
auth_sessions ──< auth_refresh_tokens
codigos_recuperacao_senha (por email)
```

- `consultas` guarda o **registro clínico da avaliação inicial** em colunas prefixadas, mapeadas
  como `@Embeddable`: `qc_*` (quadro clínico), `hv_*` (hábitos de vida), `ef_*` (exame
  físico) e `dx_*` (diagnóstico). Os textos longos são `TEXT`.
- Nenhuma chave estrangeira tem `ON DELETE CASCADE`. Excluir um profissional com pacientes,
  um paciente com consultas/mensagens ou uma consulta com evolução retorna **409**. A única
  exclusão em cascata feita pelo código é a da avaliação ao excluir a consulta.
- Identidades são `TIPO:id` (ex. `PACIENTE:42`), porque admin, profissional e paciente vivem
  em tabelas separadas e podem ter o mesmo `id`.

## Segurança

- Senhas e códigos de recuperação: **BCrypt**. Refresh tokens: 256 bits aleatórios,
  guardados apenas como hash **SHA-256**.
- JWT **RS256** com `iss`, `aud`, `sub`, `role`, `jti`, `sid`, `iat` e `exp`. Em `dev` o par
  RSA é gerado a cada subida; nos demais profiles vem de arquivos PEM.
- A cada requisição, `AuthService.authenticate` busca a sessão (`sid`), confere se não foi
  revogada nem expirou e compara o hash da senha atual da conta com o registrado no login.
  Por isso trocar a senha, redefinir a senha, editar a senha pelo admin ou excluir a conta
  derruba **imediatamente** todos os tokens daquela conta.
- O email é único entre os três tipos de conta (`auth_emails`). Na subida, o
  `EmailRegistryInitializer` reconcilia esse registro e **impede a inicialização** se encontrar
  emails duplicados entre tabelas.
- CORS: origens vindas de `app.cors.allowed-origins` (`CORS_ALLOWED_ORIGINS`); por padrão
  `http://localhost`, `https://localhost`, `capacitor://localhost` e `http://localhost:4200`.
  Métodos `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`; cabeçalhos `Authorization` e `Content-Type`.
- Respostas 401 e 403 do filtro de segurança têm corpo JSON, e as 401 trazem
  `WWW-Authenticate: Bearer` (nunca `Basic`), para o navegador não abrir o popup nativo de
  login. O 401 de credenciais erradas em `/auth/login` vem do `ApiExceptionHandler`, sem esse
  cabeçalho.

## Regras de negócio

### Contas e vínculos

1. Existe um único admin, criado na primeira subida a partir de `app.admin.*` (se a tabela
   `admins` estiver vazia).
2. Profissionais só são criados pelo admin.
3. Paciente pode ser criado pelo profissional (já nasce vinculado a ele) ou por autocadastro
   (nasce sem profissional). O admin pode trocar o profissional responsável.
4. No primeiro agendamento feito pelo paciente, se ele não tem profissional responsável,
   o profissional da consulta passa a ser o responsável. Agendamentos seguintes com outros
   profissionais não mudam o vínculo.

### Posse dos recursos

5. O profissional só acessa pacientes cujo responsável é ele, e só cria consultas para eles.
6. O paciente consulta qualquer profissional e agenda com qualquer um.
7. Acesso a recurso de outro dono retorna **404** (não 403), com a mesma mensagem de
   recurso inexistente.

### Agenda

8. A grade de disponibilidade é fixa: **08:00 a 17:30, de 30 em 30 minutos**, igual para
   todos os profissionais e todos os dias da semana.
9. Um horário está ocupado se existe consulta **não cancelada** do mesmo profissional no
   mesmo instante exato. A verificação bloqueia a linha do profissional
   (`findWithLockById`) para dois agendamentos simultâneos não pegarem o mesmo horário.
10. Paciente só cancela ou remarca consulta `AGENDADA` ou `CONFIRMADA`. Remarcar marca
    `foiRemarcada = true`.
11. Consulta particular agendada pelo paciente herda o valor do profissional; com convênio,
    fica sem valor.

### Mensagens

12. A conversa existe somente se houver **ao menos uma consulta** entre paciente e
    profissional, em qualquer status. Ter cadastrado o paciente não basta.
13. As caixas de entrada listam todos os contatos com consulta, primeiro os que têm mensagem
    (mais recente no topo) e depois os sem mensagem (ordem alfabética).

### Avaliações

14. Só consultas `REALIZADA` podem ser avaliadas, uma única vez (nota de 1 a 5). Tanto o
    paciente quanto o profissional podem registrar a avaliação.

### Prontuário

15. A evolução clínica é imutável: não existe edição nem exclusão pela API.
16. O plano terapêutico é versionado: cada novo plano é uma nova revisão, e a revisão ativa
    anterior vira `SUBSTITUIDO`. Há no máximo um plano `ATIVO` por paciente e profissional.
17. O paciente só vê evoluções e planos marcados com `visivelPaciente = true`.

### Recuperação de senha

18. Código de 6 dígitos, guardado com BCrypt, válido por 15 minutos, um código ativo por email.
19. No máximo 5 tentativas erradas por código; depois disso é preciso pedir outro.
20. A solicitação responde 204 exista ou não a conta; falha de SMTP só vai para o log.

## Profiles

| Profile | Banco | Chaves JWT | Admin | Email de recuperação |
|---|---|---|---|---|
| `dev` | H2 em memória | Par RSA efêmero | `admin@fisiotech.com` / `12345678` | Só no log |
| `homolog` | MySQL (`DB_URL`, padrão `jdbc:mysql://mysql:3306/fisiotech`) | Arquivos PEM | Variáveis `ADMIN_*` | SMTP `MAIL_*` |
| `prod` | MySQL (`DB_URL`) | Arquivos PEM | Variáveis `ADMIN_*` | SMTP `SPRING_MAIL_*` |

As variáveis de cada profile estão no [README](../README.md#variáveis-de-ambiente). O profile
`prod` tem prioridade sobre `dev`: com os dois ativos, as chaves efêmeras e o console H2 ficam
desligados.

## Testes

- **Unidade** (`*ServiceTest`): services com repositórios mockados (Mockito).
- **Integração HTTP** (`AuthIntegrationTest`, `RecuperacaoSenhaIntegrationTest`,
  `BusinessEndpointsIntegrationTest`): `@SpringBootTest` com servidor real e H2.
- **Migrações** (`FlywayMigrationTest` em H2; `FlywayMySqlIntegrationTest` em MySQL 8, ativado
  só quando `MYSQL_TEST_URL` está definida, como no CI).

A cobertura por área está na [Matriz de testes](matriz-testes.md).
