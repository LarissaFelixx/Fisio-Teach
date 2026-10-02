# Board de sprints — rastreabilidade item ↔ commit

Cada linha corresponde a um card do project. A coluna de commit é a evidência de que o item
foi entregue: ao criar a issue no GitHub Projects, cole o hash no corpo para que o card fique
ligado ao código.

Reconstruído a partir de `git log --reverse master` no repositório `fisiotech-back`.
Os itens #1, #2 e #4 da Sprint 1 foram entregues no repositório `fisiotech-front`.

---

## Sprint 1 — Fundação do aplicativo e do banco de dados

| Item | Atividade | Evidência |
|---|---|---|
| #1 | Configurar Angular mobile-first com navegação por papel | repositório `fisiotech-front` |
| #2 | Integrar Capacitor e gerar o primeiro .apk | repositório `fisiotech-front` |
| #3 | Modelar e criar o banco com as entidades base | `f812e9a` commit inicial, com entidades paciente e profissional |
| #4 | Criação de documentação e entrega de sprint | documento de entrega da Sprint 1 |

## Sprint 2 — Autenticação, papéis de acesso e domínio clínico

| Item | Atividade | Commit |
|---|---|---|
| #5 | Corrigir a senha gravada em texto puro e remover credenciais fixas no código | `0211169` |
| #6 | Criar a entidade Profissional e vincular o paciente ao profissional responsável | `2668d2e` |
| #7 | Implementar a autenticação e a autorização por papel de usuário | `a686379` |
| #8 | Expor o endpoint de identificação do usuário autenticado | `0b009dd` |
| #9 | Modelar Consulta, Mensagem e Avaliação com o registro clínico do atendimento | `ddfe47e` |
| #10 | Habilitar o CORS para o aplicativo empacotado com Capacitor | `3a1dad3` |
| #11 | Documentar o setup da API e criar os scripts de execução em modo dev | `1b2584d`, `2e95be4` |

## Sprint 3 — Autoatendimento do paciente e agendamento

| Item | Atividade | Commit |
|---|---|---|
| #12 | Criar a área de autoatendimento do paciente autenticado (`/me`) | `7f4fa97` |
| #13 | Entregar a caixa de entrada unificada de mensagens do profissional | `e4c1266` |
| #14 | Adicionar a gestão de pacientes pelo administrador | `6905062` |
| #15 | Implementar o autocadastro público de paciente | `d786f47` (Fase 2) |
| #16 | Disponibilizar a busca de profissionais e a consulta de horários livres | `84850bd` (Fase 3) |
| #17 | Permitir o agendamento da consulta pelo próprio paciente | `84850bd` (Fase 3) |
| #18 | Permitir a edição de perfil e a troca de senha pelo paciente | `84850bd` (Fase 3) |

## Sprint 4 — Ciclo do atendimento, qualidade e ajustes

| Item | Atividade | Commit |
|---|---|---|
| #19 | Implementar o cancelamento e a remarcação de consulta | `fce0759` (Fase 4) |
| #20 | Vincular as mensagens à consulta e exibir a conversa por atendimento | `fce0759` (Fase 4) |
| #21 | Substituir o popup nativo de autenticação do navegador por resposta da API | `fce0759` (Fase 4) |
| #22 | Corrigir a avaliação permitida antes de a consulta ser realizada | `80db4f7` |
| #23 | Cobrir os services principais com testes de unidade | `32428d4` |
| #24 | Tornar a senha opcional na edição de profissional e de paciente | `7e22967` (F-01, PR #2) |
| #25 | Adicionar o autoatendimento de troca de senha para profissional e admin | `d8428c2` (F-02, PR #3) |
| #26 | Atualizar a documentação e realizar a entrega das sprints | `7d2b7bd` |

---

## Como montar o board no GitHub Projects

1. Crie quatro iterações (ou um campo `Sprint` de seleção única) com os valores
   Sprint 1 a Sprint 4.
2. Abra uma issue por linha, usando o texto da coluna *Atividade* como título.
3. No corpo, cole o hash do commit — o GitHub cria o link automaticamente.
4. Mova todos os cards para a coluna **Done** e feche as issues.
5. Tire uma captura de tela por sprint e cole nas figuras 1 a 4 do documento de entrega.

Como as issues são criadas depois dos commits, elas não fecham sozinhas por mensagem de
commit; o fechamento é manual.
