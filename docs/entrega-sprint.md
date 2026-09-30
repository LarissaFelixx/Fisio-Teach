# Entrega da sprint

## Escopo entregue

| Atividade | Implementação e evidência no repositório |
|---|---|
| #5 HTTP Basic para JWT | Login, access/refresh token, rotação, logout e revogação de sessões; HTTP Basic desabilitado. |
| #6 Flyway | Migrações `V1` a `V4`, validação em H2 e teste condicional em MySQL 8. |
| #7 API em homologação e APK | Perfil `homolog`, Docker, Compose, health checks e contrato de configuração do app. A publicação da URL e a geração do APK dependem da infraestrutura e do repositório mobile. |
| #8 CI | GitHub Actions com Java 21, Maven Verify, MySQL 8 e artefatos de relatórios/JAR. |
| #9 Recuperação de senha | Solicitação neutra, token com hash/expiração/uso único, limite de solicitações, SMTP configurável e revogação das sessões. |
| #10 Evolução e plano | Evoluções imutáveis, planos terapêuticos versionados, histórico e visibilidade separada entre profissional e paciente. |
| #11 Agenda e indicadores | Pesquisa por período e filtros, bloqueio para evitar choque de horário e indicadores consolidados. |
| #12 Paginação | Resposta paginada padronizada, limite de tamanho, filtros e lista segura de campos de ordenação. |
| #13 Integração | Testes HTTP de autenticação, autorização, cadastro, paginação, consulta, agenda e indicadores, além dos testes de serviço por domínio. |
| #14 Documentação | README atualizado, Swagger, guias de JWT, homologação/APK, matriz de testes e este documento de entrega. |

## Sequência de commits

1. `dfc1faa` — migrações Flyway.
2. `26d5f89` — pipeline de CI.
3. `231d7eb` — recuperação de senha.
4. `6d9b571` — prontuário e plano terapêutico.
5. `2ecde29` — agenda e indicadores.
6. `f13ea15` — paginação, filtros e ordenação.
7. `7063a09` — testes de integração dos endpoints.
8. `df74f93` — preparação de homologação e APK.
9. O commit deste documento consolida a entrega e a documentação.

## Critérios de validação

- `mvnw.cmd test` deve finalizar sem falhas; o teste de MySQL é executado pelo CI quando as variáveis do banco estão presentes.
- A aplicação deve iniciar com Flyway e Hibernate em modo de validação.
- `/actuator/health/readiness` deve responder `UP` no ambiente implantado.
- O Swagger deve refletir os endpoints publicados.
- O APK deve apontar para a URL HTTPS de homologação e ser validado em dispositivo ou emulador.

## Limites externos

O código comprova a preparação reproduzível para homologação e para integração do aplicativo. A URL pública, o certificado TLS, o serviço SMTP, o repositório mobile, a assinatura Android e o aceite formal da sprint são evidências externas e devem ser anexadas pela equipe responsável quando forem executadas.
