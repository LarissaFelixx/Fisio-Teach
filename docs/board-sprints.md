# Board de sprints — rastreabilidade item ↔ commit

Liga cada atividade ao commit que a entrega. Há duas fontes de numeração, e este documento
separa as duas:

1. **Board atual do `Fisio-Teach`** (GitHub Projects de
   [LarissaFelixx/Fisio-Teach](https://github.com/LarissaFelixx/Fisio-Teach)): os números são
   **issues reais**. As issues #1 a #16 estão na coluna *Done - SPRINT 1 e 2*.
2. **Histórico do repositório original `fisiotech-back`**, integrado ao `Fisio-Teach` no PR #31:
   as atividades foram reconstruídas a partir de `git log --reverse master` e numeradas no
   documento `docs/FisioTech_Documentacao_Entrega_Sprints.docx` como "Sprints 1 a 4". Esses
   itens **não são issues** do GitHub; aqui eles usam o prefixo `FB-` para não colidir com as
   issues reais (ex. `FB-05` = item #5 do `.docx`).

---

## Board atual (`Fisio-Teach`)

### Sprint 1 — Fundação do aplicativo e do banco de dados

| Issue | Atividade | Evidência |
|---|---|---|
| #1 | Configurar Angular mobile-first com navegação por papel | repositório do front-end |
| #2 | Integrar Capacitor e gerar o primeiro .apk | repositório do front-end |
| #3 | Modelar e criar o banco com as entidades base | `f812e9a` — commit inicial, com entidades paciente e profissional |
| #4 | Criação de documentação e entrega de sprint | `docs/FisioTech_Documentacao_Entrega_Sprints.docx` |

### Sprint 2 — Segurança, infraestrutura e evolução clínica

Detalhes em [Entrega da Sprint 2](entrega-sprint.md).

| Issue | Atividade | Commit | Estado no GitHub |
|---|---|---|---|
| #5 | Migrar a autenticação de HTTP Basic para JWT | `1928362` | Fechada |
| #6 | Versionar o schema do banco com migrações Flyway | `dfc1faa`, `cc9d187` | Fechada (#7 é duplicata aberta) |
| #8 | Publicar a API em ambiente de homologação e gerar o .apk | `df74f93` | Fechada (#9 é duplicata aberta) — URL pública e APK dependem de infraestrutura externa |
| #10 | Automatizar build e testes em pipeline de integração contínua | `26d5f89`, `6db1043` | Fechada |
| #11 | Implementar a recuperação de senha por e-mail | `231d7eb`, `df407dd`, `9197b85` | Fechada |
| #12 | Registrar a evolução clínica e o plano terapêutico por sessão | `6d9b571` | Fechada |
| #13 | Construir a agenda e os indicadores do profissional | `2ecde29` | Fechada |
| #14 | Paginar, filtrar e ordenar as listagens da API e do aplicativo | `f13ea15` | Fechada |
| #15 | Cobrir os endpoints com testes de integração automatizados | `7063a09` | Fechada |
| #16 | Criação de documentação e entrega de sprint | `6f43585`, `fa6577e` | Fechada |

### Outras issues do board

- #18 e #19: duplicatas fechadas de #3.
- #17 (migração do front para React Native), #20, #21 e #25 a #29 (telas do front-end): trabalho
  do front-end, fora deste repositório.
- #30 (documento técnico, item 3.3): coluna *DONE - SPRINT 3*.

---

## Histórico do repositório original `fisiotech-back`

Atividades entregues no `fisiotech-back` antes da integração, na divisão usada pelo `.docx`.
Os itens `FB-01`, `FB-02` e `FB-04` correspondem às issues #1, #2 e #4 do board atual.

### Etapa 1 — Fundação do aplicativo e do banco de dados (Sprint 1 do `.docx`)

Mesmo conteúdo da Sprint 1 do board atual (issues #1 a #4).

### Etapa 2 — Autenticação, papéis de acesso e domínio clínico (Sprint 2 do `.docx`)

| Item | Atividade | Commit |
|---|---|---|
| FB-05 | Corrigir a senha gravada em texto puro e remover credenciais fixas no código | `0211169` |
| FB-06 | Criar a entidade Profissional e vincular o paciente ao profissional responsável | `2668d2e` |
| FB-07 | Implementar a autenticação e a autorização por papel de usuário | `a686379` |
| FB-08 | Expor o endpoint de identificação do usuário autenticado | `0b009dd` |
| FB-09 | Modelar Consulta, Mensagem e Avaliação com o registro clínico do atendimento | `ddfe47e` |
| FB-10 | Habilitar o CORS para o aplicativo empacotado com Capacitor | `3a1dad3` |
| FB-11 | Documentar o setup da API e criar os scripts de execução em modo dev | `1b2584d`, `2e95be4` |

### Etapa 3 — Autoatendimento do paciente e agendamento (Sprint 3 do `.docx`)

| Item | Atividade | Commit |
|---|---|---|
| FB-12 | Criar a área de autoatendimento do paciente autenticado (`/me`) | `7f4fa97` |
| FB-13 | Entregar a caixa de entrada unificada de mensagens do profissional | `e4c1266` |
| FB-14 | Adicionar a gestão de pacientes pelo administrador | `6905062` |
| FB-15 | Implementar o autocadastro público de paciente | `d786f47` (Fase 2) |
| FB-16 | Disponibilizar a busca de profissionais e a consulta de horários livres | `84850bd` (Fase 3) |
| FB-17 | Permitir o agendamento da consulta pelo próprio paciente | `84850bd` (Fase 3) |
| FB-18 | Permitir a edição de perfil e a troca de senha pelo paciente | `84850bd` (Fase 3) |

### Etapa 4 — Ciclo do atendimento, qualidade e ajustes (Sprint 4 do `.docx`)

| Item | Atividade | Commit |
|---|---|---|
| FB-19 | Implementar o cancelamento e a remarcação de consulta | `fce0759` (Fase 4) |
| FB-20 | Vincular as mensagens à consulta e exibir a conversa por atendimento | `fce0759` (Fase 4) |
| FB-21 | Substituir o popup nativo de autenticação do navegador por resposta da API | `fce0759` (Fase 4) |
| FB-22 | Corrigir a avaliação permitida antes de a consulta ser realizada | `80db4f7` |
| FB-23 | Cobrir os services principais com testes de unidade | `32428d4` |
| FB-24 | Tornar a senha opcional na edição de profissional e de paciente | `7e22967` (F-01, PR #2 do `fisiotech-back`) |
| FB-25 | Adicionar o autoatendimento de troca de senha para profissional e admin | `d8428c2` (F-02, PR #3 do `fisiotech-back`) |
| FB-26 | Atualizar a documentação e realizar a entrega das sprints | `7d2b7bd` |

---

## Mantendo o board

1. Ao criar uma issue para uma atividade já entregue, cole o hash do commit no corpo — o GitHub
   cria o link automaticamente.
2. Como as issues são criadas depois dos commits, elas não fecham sozinhas por mensagem de
   commit; feche manualmente e mova o card para a coluna *Done* da sprint.
3. Se as etapas do histórico `fisiotech-back` virarem issues, troque o `FB-NN` desta tabela pelo
   número real da issue.
4. As figuras 1 a 4 do `.docx` esperam uma captura de tela do board por etapa.
