# Matriz de testes de integração

| Área | Cenários | Estado |
|---|---|---|
| Autenticação | login por papel, token inválido, renovação, logout, revogação e CORS | Automatizado |
| Migrações | banco vazio, reexecução e MySQL 8 | Automatizado |
| Profissionais | CRUD, validação e autorização | Unitário; integração pendente |
| Pacientes | cadastro, CRUD, isolamento e perfil | Parcial |
| Consultas | agendamento, conflito, atualização, cancelamento e isolamento | Unitário; integração pendente |
| Mensagens | envio, conversa, caixa de entrada e vínculo por consulta | Unitário; integração pendente |
| Avaliações | consulta realizada, duplicidade e isolamento | Unitário; integração pendente |
| Recuperação de senha | resposta neutra, limite, consumo e revogação | Automatizado |
| Prontuário | evolução imutável, revisão do plano, histórico e visibilidade | Automatizado no serviço; HTTP na etapa de integração |
| Agenda e indicadores | período, filtros, conflito uniforme e consolidação | Automatizado no serviço; HTTP na etapa de integração |
| Paginação | limites, filtros, ordenação e isolamento | Pendente da funcionalidade |
