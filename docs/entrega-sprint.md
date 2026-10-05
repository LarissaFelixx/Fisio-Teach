# Entrega da Sprint 2

A Sprint 2 do board do `Fisio-Teach` implementou as melhorias levantadas no
[backlog](backlog-futuro.md) após o ciclo do repositório original `fisiotech-back` (Etapas 1 a 4
em [Board de sprints](board-sprints.md#histórico-do-repositório-original-fisiotech-back)). Os
números abaixo são as **issues reais** do repositório
[LarissaFelixx/Fisio-Teach](https://github.com/LarissaFelixx/Fisio-Teach/issues), todas na coluna
*Done - SPRINT 1 e 2* do board.

## Escopo entregue

| Issue | Atividade | Implementação e evidência no repositório | Commits |
|---|---|---|---|
| #5 | Migrar a autenticação de HTTP Basic para JWT | Login, access token RS256 de 15 min, refresh token com rotação e detecção de reutilização, logout, revogação de sessões na troca de senha e exclusão de conta; HTTP Basic desabilitado | `1928362` |
| #6 | Versionar o schema com migrações Flyway | Migrações `V1` a `V5`, `ddl-auto=validate` em todos os profiles, teste em H2 e teste condicional em MySQL 8 | `dfc1faa`, `cc9d187` |
| #8 | Publicar a API em homologação e gerar o .apk | Profile `homolog`, `Dockerfile`, `compose.homolog.yml`, health checks, CORS por variável e contrato de configuração do app. A publicação da URL e a geração do APK dependem da infraestrutura e do repositório mobile | `df74f93` |
| #10 | Automatizar build e testes no CI | GitHub Actions com JDK 21, `mvnw verify`, MySQL 8.4 de serviço, relatório de testes por classe (check + comentário na PR) e artefatos de relatório e jar | `26d5f89`, `0df499d`…`6db1043` |
| #11 | Implementar a recuperação de senha por email | Código de 6 dígitos com hash BCrypt, validade de 15 min, um código ativo por email, limite de 5 tentativas, resposta neutra (sempre 204), SMTP configurável e revogação das sessões | `231d7eb`, `df407dd`, `9197b85`, `d793803` |
| #12 | Registrar a evolução clínica e o plano terapêutico | Evoluções imutáveis, planos terapêuticos versionados (um ativo), visibilidade controlada para o paciente | `6d9b571` |
| #13 | Construir a agenda e os indicadores do profissional | Consulta por período com filtros de paciente, status e tipo; bloqueio de horário duplicado; total, pacientes atendidos, taxa de cancelamento e distribuição por status | `2ecde29` |
| #14 | Paginar, filtrar e ordenar as listagens | `PageResponse` padronizada, `size` limitado a 100, filtros e lista permitida de campos de ordenação | `f13ea15` |
| #15 | Cobrir os endpoints com testes de integração | Testes HTTP de autenticação, autorização, isolamento, cadastro, paginação, consulta, agenda, indicadores e recuperação de senha | `7063a09` |
| #16 | Documentação e entrega da sprint | README, Swagger, guias de JWT e homologação, matriz de testes e este documento | `6f43585`, `fa6577e`, `9904898` |

As issues `#7` (Flyway) e `#9` (homologação) são duplicatas de `#6` e `#8` e continuam abertas
no GitHub.

## Sequência de commits

1. `1928362` — JWT com sessões revogáveis.
2. `dfc1faa` — migrações Flyway.
3. `26d5f89` — pipeline de CI.
4. `231d7eb` — recuperação de senha (primeira versão, por link com token).
5. `6d9b571` — prontuário e plano terapêutico.
6. `2ecde29` — agenda e indicadores.
7. `f13ea15` — paginação, filtros e ordenação.
8. `7063a09` — testes de integração dos endpoints.
9. `df407dd`, `9197b85` — recuperação de senha por código de 6 dígitos, integrada ao JWT.
10. `df74f93` — preparação de homologação e APK.
11. `6f43585` — consolidação da documentação.
12. `0df499d`…`6db1043` — relatório de testes legível no CI.
13. `d793803`, `fa6577e`, `9904898`, `cc9d187` — integração no repositório `Fisio-Teach`
    (PR #31), migração `V5` (troca da tabela de tokens por `codigos_recuperacao_senha`) e
    correção da `V1` no MySQL (textos clínicos longos como `TEXT`).

## Critérios de validação

| Critério | Como verificar | Situação |
|---|---|---|
| Suíte de testes sem falhas | `mvnw.cmd test` / `./mvnw test` | 109 testes, 0 falhas; o teste em MySQL roda no CI |
| Subida com Flyway e Hibernate em validação | Subir com `dev` ou `homolog` | Coberto por `FlywayMigrationTest` e pela subida dos testes de integração |
| Saúde do ambiente | `GET /actuator/health/readiness` = `UP` | Configurado; depende do ambiente implantado |
| Swagger reflete os endpoints | `/swagger-ui.html` | Gerado do código |
| APK apontando para a homologação HTTPS | Instalar em dispositivo/emulador | Depende de infraestrutura externa (issue `#8`) |

## Limites externos

O código comprova a preparação reproduzível para homologação e para a integração do aplicativo.
A URL pública, o certificado TLS, o serviço SMTP, o repositório mobile, a assinatura Android e o
aceite formal da sprint são evidências externas e devem ser anexados pela equipe responsável
quando forem executados.
