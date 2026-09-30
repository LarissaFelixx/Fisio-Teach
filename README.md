# FisioTech — Backend

API REST para um sistema de gestão de clínica de fisioterapia. Permite que um **administrador** cadastre **profissionais**, que cada profissional gerencie seus próprios **pacientes**, **consultas**, **mensagens** e **avaliações**, e que o próprio **paciente** acompanhe seu tratamento e converse com o profissional através de uma área de autoatendimento (`/me`).

Este documento cobre tudo que é necessário para clonar o projeto em qualquer máquina, rodá-lo localmente e testá-lo via HTTP (curl/Postman) ou junto com o front-end ([FisioTech-front](https://github.com/gabrielneriqa/fisiotech-front)).

## Stack

- **Java 21**
- **Spring Boot 4.0.5** (Web MVC, Data JPA, Security, Validation)
- **Maven** (usa o wrapper `mvnw`/`mvnw.cmd`, não é necessário ter o Maven instalado)
- **H2** (banco em memória, usado em desenvolvimento)
- **MySQL** (banco relacional, usado em produção via `mysql-connector-j`)
- **springdoc-openapi** (Swagger UI gerado automaticamente)
- **Lombok**

## Pré-requisitos

- **JDK 21** instalado e no `PATH` (ou configurado via `JAVA_HOME`)
- Nenhuma instalação de Maven é necessária — use os scripts `./mvnw` (Linux/macOS/Git Bash) ou `mvnw.cmd` (Windows/PowerShell/CMD) incluídos no repositório
- Para produção: uma instância MySQL acessível
- Porta livre para o servidor (padrão `8080` — veja a seção [Portas](#portas) se ela já estiver em uso na sua máquina)

## Clonando e rodando

```bash
git clone https://github.com/gabrielneriqa/fisiotech-back.git
cd fisiotech-back
```

O projeto tem dois profiles Spring relevantes para rodar localmente:

- **default** (`application.properties`): só define o nome da aplicação. Sozinho, sem um profile adicional, a aplicação sobe mas **não tem usuário admin nem H2 console habilitados** — normalmente você vai querer combiná-lo com o profile `dev`.
- **dev** (`application-dev.properties`): habilita o console do H2, o `show-sql` e cria automaticamente um usuário **admin** com credenciais fixas (`admin@fisiotech.com` / `12345678`) — é o profile recomendado para desenvolvimento e testes locais.

Para rodar em modo dev (banco H2 em memória, sem precisar de nenhum banco externo), o jeito mais rápido é usar o script incluído no repositório:

```bash
# Linux/macOS/Git Bash
./dev.sh          # porta 8080 por padrão
./dev.sh 8081     # ou informe outra porta

# Windows PowerShell
.\dev.ps1
.\dev.ps1 -Port 8081
```

Isso equivale a rodar diretamente:

```bash
# Linux/macOS/Git Bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Windows (PowerShell/CMD)
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

Alternativamente, defina a variável de ambiente antes de subir a aplicação:

```bash
# Linux/macOS/Git Bash
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

```powershell
# Windows PowerShell
$env:SPRING_PROFILES_ACTIVE = "dev"
mvnw.cmd spring-boot:run
```

A API sobe em `http://localhost:8080` (a menos que a porta seja alterada — veja abaixo). Como o banco é H2 **em memória**, todo o estado é perdido ao reiniciar a aplicação — isso é esperado em dev.

### Portas

O servidor usa a porta `8080` por padrão. Se ela já estiver ocupada na sua máquina (é comum em Windows, por exemplo com o NVIDIA Broadcast), suba em outra porta:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev -Dspring-boot.run.arguments=--server.port=8081
```

Se você mudar a porta, lembre de ajustar também o `proxy.conf.js` do front-end (veja o README do [FisioTech-front](https://github.com/gabrielneriqa/fisiotech-front)) para apontar para a porta correta.

### Rodando os testes

```bash
./mvnw test
```

## Console do H2 (modo dev)

Com o profile `dev` ativo, o console web do H2 fica disponível em:

```
http://localhost:8080/h2-console
```

Use a JDBC URL exibida no log de inicialização da aplicação (procure por uma linha contendo `jdbc:h2:mem:`) — usuário `sa`, sem senha.

## Documentação interativa da API (Swagger)

Com a aplicação rodando, a documentação OpenAPI/Swagger fica disponível em:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- JSON da spec: `http://localhost:8080/v3/api-docs`

Esses dois caminhos, assim como `/h2-console/**`, são liberados sem autenticação; todo o resto da API exige HTTP Basic Auth.

## Autenticação e papéis

A API usa **HTTP Basic Auth** (usuário = email, senha = senha cadastrada) e tem três papéis:

| Papel | Prefixo de rotas protegidas | Quem gerencia |
|---|---|---|
| `ROLE_ADMIN` | `/profissionais/**`, `/admin/pacientes/**` | Único (seed do profile `dev`, ou variáveis de ambiente em prod) |
| `ROLE_PROFISSIONAL` | `/pacientes/**`, `/consultas/**`, `/mensagens/**`, `/avaliacoes/**` | Cadastrado pelo admin |
| `ROLE_PACIENTE` | `/me/**` | Cadastrado pelo profissional responsável, ou autocadastrado via `POST /pacientes/cadastro` (rota pública, sem autenticação — o único paciente cria a própria conta sem vínculo com nenhum profissional até marcar a primeira consulta) |

Todo recurso é isolado por dono: um profissional só enxerga os próprios pacientes/consultas, e tentar acessar um recurso de outro profissional retorna **404** (não 403, para não confirmar a existência do recurso a quem não tem acesso). **Mensagens são a exceção**: a posse de uma conversa não é "esse paciente é meu", e sim "existe consulta marcada entre esse paciente e esse profissional" (qualquer status, inclusive cancelada) — então um profissional só consegue trocar mensagem com um paciente depois que existir pelo menos uma consulta entre os dois, mesmo que o profissional tenha sido quem cadastrou aquele paciente.

Em modo `dev`, o admin já vem pronto:

```
email: admin@fisiotech.com
senha: 12345678
```

Em produção (profile `prod`), o admin é criado a partir das variáveis de ambiente `ADMIN_NOME`, `ADMIN_EMAIL` e `ADMIN_SENHA` (veja [Deploy em produção](#deploy-em-produção-profile-prod)) — não existe usuário admin fixo em produção.

## Testando a API do zero (fluxo completo via curl)

Com a aplicação rodando em modo `dev` (`http://localhost:8080`), este é o caminho completo para popular dados e testar as três camadas de autenticação — incluindo o autocadastro de paciente, a busca/agendamento de consulta pelo próprio paciente e a troca de mensagens (que **exige uma consulta marcada entre as partes**, veja [Autenticação e papéis](#autenticação-e-papéis)).

**1. Admin cria um profissional:**

```bash
curl -u admin@fisiotech.com:12345678 -X POST http://localhost:8080/profissionais \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Ana Souza",
    "email": "ana@fisiotech.com",
    "senha": "senha123",
    "registroProfissional": "CREFITO-11111",
    "especialidade": "Ortopedia",
    "valorConsultaParticular": 150.00,
    "conveniosAceitos": ["Unimed", "Bradesco Saúde"]
  }'
```

**2. Confirma o login do profissional:**

```bash
curl -u ana@fisiotech.com:senha123 http://localhost:8080/auth/me
```

**3. Paciente se autocadastra (rota pública, sem autenticação — ainda não tem nenhum profissional vinculado):**

```bash
curl -X POST http://localhost:8080/pacientes/cadastro \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Joao Silva",
    "email": "joao@paciente.com",
    "senha": "senha123"
  }'
```

**4. Paciente confirma o login, busca profissionais disponíveis e consulta a disponibilidade de horários (use o `id` retornado no passo 1, normalmente `1`):**

```bash
curl -u joao@paciente.com:senha123 http://localhost:8080/auth/me
curl -u joao@paciente.com:senha123 "http://localhost:8080/me/profissionais?especialidade=Ortopedia"
curl -u joao@paciente.com:senha123 "http://localhost:8080/me/profissionais/1/disponibilidade?data=2026-08-20"
```

**5. Paciente marca uma consulta (isso vincula automaticamente esse profissional como responsável, já que é a primeira consulta do paciente):**

```bash
curl -u joao@paciente.com:senha123 -X POST http://localhost:8080/me/consultas \
  -H "Content-Type: application/json" \
  -d '{
    "profissionalId": 1,
    "dataHora": "2026-08-20T09:00:00",
    "tipo": "PRESENCIAL",
    "convenio": null
  }'
```

**6. Profissional confere que o paciente aparece na sua lista (use o `id` do paciente retornado no passo 3, normalmente `1`) e envia uma mensagem — agora funciona, pois já existe uma consulta entre os dois:**

```bash
curl -u ana@fisiotech.com:senha123 http://localhost:8080/pacientes
curl -u ana@fisiotech.com:senha123 -X POST http://localhost:8080/mensagens \
  -H "Content-Type: application/json" \
  -d '{"pacienteId": 1, "autor": "PROFISSIONAL", "conteudo": "Oi Joao, como vai o tratamento?"}'
```

**7. Paciente lista suas conversas e lê a mensagem (use o `id` do profissional, normalmente `1`):**

```bash
curl -u joao@paciente.com:senha123 http://localhost:8080/me/mensagens/caixa-entrada
curl -u joao@paciente.com:senha123 http://localhost:8080/me/mensagens/1
```

**8. Paciente responde:**

```bash
curl -u joao@paciente.com:senha123 -X POST http://localhost:8080/me/mensagens/1 \
  -H "Content-Type: application/json" \
  -d '{"conteudo": "Oi Ana, tudo certo!"}'
```

**9. Profissional confere a conversa completa e a caixa de entrada unificada:**

```bash
curl -u ana@fisiotech.com:senha123 "http://localhost:8080/mensagens?pacienteId=1"
curl -u ana@fisiotech.com:senha123 http://localhost:8080/mensagens/caixa-entrada
```

**10. Paciente remarca a consulta e depois troca a senha:**

```bash
curl -u joao@paciente.com:senha123 -X PUT http://localhost:8080/me/consultas/1/remarcar \
  -H "Content-Type: application/json" \
  -d '{"novaDataHora": "2026-08-21T10:00:00"}'

curl -u joao@paciente.com:senha123 -X PUT http://localhost:8080/me/senha \
  -H "Content-Type: application/json" \
  -d '{"senhaAtual": "senha123", "novaSenha": "novaSenha123"}'
```

(`PUT /me/consultas/{id}/cancelar` cancela em vez de remarcar; não canceladas aqui só para manter os dados do restante do fluxo. `POST /pacientes` — sem `/cadastro` — continua existindo para o profissional cadastrar um paciente diretamente, útil para o admin/profissional criar contas de teste sem depender do autocadastro; a diferença é que um paciente cadastrado assim já nasce vinculado ao profissional que o criou, mas *ainda assim* só consegue trocar mensagem com ele depois de existir uma consulta marcada entre os dois — a regra de mensagens é sempre "existe consulta", não "quem cadastrou".)

Para os demais recursos (`/consultas`, `/avaliacoes`, `/admin/pacientes`) e os corpos de requisição exatos de cada endpoint, consulte o Swagger UI — ele reflete sempre o estado atual do código.

## Estrutura do projeto

Organização por *domínio/feature* (não por camada técnica) dentro de `src/main/java/com/app/fisiotech`:

```
admin/          # configuração de segurança (SecurityConfig), seed do admin
auth/           # endpoint /auth/me, AuthenticatedUser, UserDetailsService
profissional/   # entidade, controller, service, repository e DTOs do Profissional
paciente/       # idem para Paciente
consulta/       # idem para Consulta (com sub-objetos @Embeddable: quadro clínico, hábitos de vida, exame físico, diagnóstico)
mensagem/       # idem para Mensagem (inclui a caixa de entrada unificada)
avaliacao/      # idem para Avaliação (feita pelo paciente sobre uma consulta)
me/             # endpoints de autoatendimento do paciente logado
exception/      # tratamento global de exceções (ApiExceptionHandler)
```

Cada pacote de domínio segue o padrão `controller` → `service` → `repository` → `entity`/`dto`.

## Deploy em produção (profile `prod`)

O profile `prod` (`application-prod.properties`) espera as seguintes variáveis de ambiente:

| Variável | Descrição |
|---|---|
| `DB_URL` | URL JDBC do MySQL, ex: `jdbc:mysql://host:3306/fisiotech` |
| `DB_USERNAME` | Usuário do MySQL |
| `DB_PASSWORD` | Senha do MySQL |
| `ADMIN_NOME` | Nome do usuário admin a ser criado na primeira subida |
| `ADMIN_EMAIL` | Email do admin |
| `ADMIN_SENHA` | Senha do admin (mínimo 8 caracteres) |

Em produção o schema é validado (`ddl-auto=validate`), não gerado automaticamente — garanta que o schema MySQL já exista com as tabelas corretas antes de subir a aplicação (ou gere-o rodando a aplicação uma vez fora do profile `prod`, contra o mesmo banco, e ajustando conforme necessário).

## Solução de problemas comuns (Windows)

- **Erro de certificado TLS ao baixar dependências** (`certificate_unknown`, `PKIX path building failed`) — comum em redes corporativas/escolares com inspeção de TLS. Contorne definindo `MAVEN_OPTS`:
  ```powershell
  $env:MAVEN_OPTS = "-Djavax.net.ssl.trustStoreType=Windows-ROOT"
  ```
- **Porta 8080 já em uso** — veja [Portas](#portas) acima.
- **`JAVA_HOME` não definido** — se o `mvnw`/`mvnw.cmd` reclamar de não achar o JDK, aponte `JAVA_HOME` para a instalação do JDK 21 (ex: `$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"` ou o caminho de um JDK instalado via Gradle/IDE).
