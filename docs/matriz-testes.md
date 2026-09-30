# Matriz de testes de integração

| Área | Cenários | Estado |
|---|---|---|
| Autenticação | login por papel, token inválido, renovação, logout, revogação e CORS | Automatizado |
| Migrações | banco vazio, reexecução e MySQL 8 | Automatizado |
| Profissionais | cadastro, login, validação e autorização | Unitário e integração HTTP |
| Pacientes | cadastro, paginação, isolamento e perfil | Unitário e integração HTTP |
| Consultas | agendamento, conflito, atualização, cancelamento e isolamento | Unitário e fluxo HTTP principal |
| Mensagens | envio, conversa, caixa de entrada e vínculo por consulta | Unitário; integração pendente |
| Avaliações | consulta realizada, duplicidade e isolamento | Unitário; integração pendente |
| Recuperação de senha | resposta neutra, limite, consumo e revogação | Automatizado |
| Prontuário | evolução imutável, revisão do plano, histórico e visibilidade | Automatizado no serviço; HTTP na etapa de integração |
| Agenda e indicadores | período, filtros, conflito uniforme e consolidação | Serviço e integração HTTP |
| Paginação | limites, filtros, ordenação e isolamento | Unitário e integração HTTP |

Os cenários HTTP transversais estão em `AuthIntegrationTest` e `BusinessEndpointsIntegrationTest`. As regras específicas de mensagens, avaliações e prontuário também permanecem cobertas por testes de serviço.
