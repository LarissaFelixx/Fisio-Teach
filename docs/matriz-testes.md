# Matriz de testes

Situação verificada com `./mvnw test`: **109 testes em 20 classes, 0 falhas, 1 ignorado**
(`FlywayMySqlIntegrationTest`, que só roda com `MYSQL_TEST_URL` definida — no CI ele roda).

## Cobertura por área

| Área | Cenários | Unidade (service) | Integração HTTP |
|---|---|---|---|
| Autenticação | Login por papel, credenciais inválidas, Basic rejeitado, token inválido/expirado, emissor/destinatário/papel/assinatura, renovação com rotação, reutilização revogando a sessão, renovação concorrente, logout idempotente, CORS | `JwtConfigTest`, `AppUserDetailsServiceTest` | `AuthIntegrationTest` |
| Revogação de sessão | Troca de senha (paciente, profissional, admin), edição administrativa de senha, exclusão de conta | Troca de senha em `AdminServiceTest`, `PacienteServiceTest`, `ProfissionalServiceTest` | `AuthIntegrationTest` |
| Email único global | Conflito entre tipos de conta na criação e na edição, base legada com duplicidade | — | `AuthIntegrationTest` |
| Recuperação de senha | Resposta neutra, corpo inválido retorna 400, precedência igual à do login, código expirado, limite de 5 tentativas, uso único, código anterior invalidado, revogação das sessões | `RecuperacaoSenhaServiceTest` | `RecuperacaoSenhaIntegrationTest` |
| Papéis e isolamento | Cada papel restrito às suas rotas; paciente sem acesso a rotas de profissional; profissional sem acesso a pacientes de outro | serviços de cada domínio | `AuthIntegrationTest`, `BusinessEndpointsIntegrationTest` |
| Profissionais | Cadastro, email/registro duplicado, edição com senha opcional, troca de senha | `ProfissionalServiceTest` | `BusinessEndpointsIntegrationTest` (cadastro) |
| Pacientes | Cadastro pelo profissional, autocadastro sem vínculo, email normalizado, isolamento, edição com senha opcional, troca de senha (a gestão pelo admin não tem teste dedicado) | `PacienteServiceTest` | `BusinessEndpointsIntegrationTest` (autocadastro e paginação) |
| Consultas | Agendamento, conflito de horário, valor particular × convênio, remarcação, cancelamento, vínculo no primeiro agendamento, isolamento (a atualização pelo profissional não tem teste dedicado) | `ConsultaServiceTest` | `BusinessEndpointsIntegrationTest` (agendamento) |
| Disponibilidade | Grade de horários e horários ocupados | `DisponibilidadeServiceTest` | — |
| Agenda e indicadores | Período, filtros e consolidação dos indicadores | `AgendaServiceTest` | `BusinessEndpointsIntegrationTest` |
| Paginação | Limites de página/tamanho, ordenação permitida, filtros | `PageRequestFactoryTest` | `BusinessEndpointsIntegrationTest` |
| Mensagens | Envio, conversa, caixa de entrada, exigência de consulta entre as partes | `MensagemServiceTest` | **Pendente** |
| Avaliações | Só consulta realizada, duplicidade, isolamento | `AvaliacaoServiceTest` | **Pendente** |
| Prontuário | Nova revisão do plano substitui a anterior | `ProntuarioServiceTest` | **Pendente** |
| Migrações | Banco vazio em H2; banco MySQL 8 | `FlywayMigrationTest`, `FlywayMySqlIntegrationTest` | — |
| Swagger | Esquema `bearerAuth` e login público documentados | — | `AuthIntegrationTest` |
| Infraestrutura de testes | Nomes de teste em frase nos relatórios | `FraseDisplayNameGeneratorTest` | — |

## Testes por classe

| Classe | Tipo | Testes |
|---|---|---|
| `AuthIntegrationTest` | Integração HTTP | 16 |
| `ConsultaServiceTest` | Unidade | 13 |
| `PacienteServiceTest` | Unidade | 11 |
| `ProfissionalServiceTest` | Unidade | 11 |
| `RecuperacaoSenhaServiceTest` | Unidade | 11 |
| `AvaliacaoServiceTest` | Unidade | 9 |
| `FraseDisplayNameGeneratorTest` | Unidade | 6 |
| `MensagemServiceTest` | Unidade | 6 |
| `AdminServiceTest` | Unidade | 5 |
| `AppUserDetailsServiceTest` | Unidade | 5 |
| `DisponibilidadeServiceTest` | Unidade | 3 |
| `RecuperacaoSenhaIntegrationTest` | Integração HTTP | 3 |
| `BusinessEndpointsIntegrationTest` | Integração HTTP | 2 (fluxos longos) |
| `JwtConfigTest` | Unidade | 2 |
| `AgendaServiceTest` | Unidade | 1 |
| `FisiotechApplicationTests` | Subida do contexto | 1 |
| `FlywayMigrationTest` | Migração (H2) | 1 |
| `FlywayMySqlIntegrationTest` | Migração (MySQL) | 1 (condicional) |
| `PageRequestFactoryTest` | Unidade | 1 |
| `ProntuarioServiceTest` | Unidade | 1 |
| **Total** | | **109** |

As lacunas marcadas como **Pendente** estão registradas no
[Backlog](backlog-futuro.md#pendências-atuais) (item P6).
