# Homologação da API e integração do APK

## O que o repositório fornece

| Arquivo | Papel |
|---|---|
| `Dockerfile` | Build em duas etapas (JDK 21 → JRE 21), executa como usuário sem privilégios (`uid 10001`) e sobe com `--spring.profiles.active=homolog` na porta 8080 |
| `compose.homolog.yml` | Serviços `mysql` (MySQL 8.4 com volume `mysql-data` e healthcheck) e `api` (depende do MySQL saudável; healthcheck em `/actuator/health/readiness`) |
| `.env.homolog.example` | Modelo das variáveis lidas pelo serviço `api` |
| `application-homolog.properties` | Mapeia as variáveis para banco, admin, JWT, SMTP e CORS |

## Subindo a API

1. Copie o modelo e preencha os segredos. O arquivo `.env.homolog` está no `.gitignore` e
   **não deve ser commitado**.

   ```bash
   cp .env.homolog.example .env.homolog
   ```

2. Gere o par de chaves JWT fora do repositório:

   ```bash
   mkdir -p keys
   openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out keys/private.pem
   openssl pkey -in keys/private.pem -pubout -out keys/public.pem
   ```

3. Torne as chaves visíveis ao contêiner. Os valores de exemplo
   `classpath:keys/*.pem` **não funcionam**: `*.pem` é ignorado pelo Git e pelo
   `.dockerignore`, então os arquivos nunca entram na imagem. Use `file:` e monte a pasta
   como volume no serviço `api`, por exemplo com um `compose.override.yml` local:

   ```yaml
   services:
     api:
       volumes:
         - ./keys:/run/keys:ro
   ```

   e no `.env.homolog`:

   ```dotenv
   JWT_PRIVATE_KEY=file:/run/keys/private.pem
   JWT_PUBLIC_KEY=file:/run/keys/public.pem
   ```

   O contêiner roda com `uid 10001`; garanta que esse usuário consiga ler os arquivos.

4. Suba os serviços:

   ```bash
   docker compose -f compose.homolog.yml -f compose.override.yml up --build -d
   ```

5. Verifique:
   - `GET http://<host>:8080/actuator/health/readiness` → `{"status":"UP"}`;
   - `GET /actuator/info` mostra `"environment": "homolog"`;
   - Swagger em `/swagger-ui.html`;
   - login do admin definido em `ADMIN_EMAIL` / `ADMIN_PASSWORD`.

Na subida, o Flyway cria ou atualiza o schema (V1–V5), o Hibernate valida as entidades e o
admin é criado se a tabela `admins` estiver vazia.

## Variáveis do `.env.homolog`

| Variável | Exemplo | Observação |
|---|---|---|
| `DB_PASSWORD` | `trocar` | Usada pelo MySQL (`MYSQL_PASSWORD`) e pela API |
| `DB_ROOT_PASSWORD` | `trocar` | Senha root do MySQL do Compose |
| `DB_URL` | — | Opcional; padrão `jdbc:mysql://mysql:3306/fisiotech?createDatabaseIfNotExist=true&serverTimezone=UTC` |
| `DB_USER` | — | Opcional; padrão `fisiotech` (mesmo usuário criado pelo Compose) |
| `ADMIN_NAME` | — | Opcional; padrão `Administrador` |
| `ADMIN_EMAIL` | `admin@homolog.example` | Obrigatória |
| `ADMIN_PASSWORD` | segredo forte | Obrigatória, mínimo 8 caracteres |
| `JWT_PRIVATE_KEY` / `JWT_PUBLIC_KEY` | `file:/run/keys/private.pem` | Obrigatórias; ver passo 3 |
| `MAIL_HOST` | `smtp.example` | Declarar (pode ficar sem SMTP real; o envio falha só no log) |
| `MAIL_PORT` | — | Opcional; padrão `587` |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | vazio | Declarar, mesmo vazias |
| `MAIL_SMTP_AUTH` / `MAIL_STARTTLS` | — | Opcionais; padrão `true` |
| `MAIL_FROM` | `no-reply@homolog.example` | Remetente do código de recuperação |
| `CORS_ALLOWED_ORIGINS` | `https://app-homolog.example,capacitor://localhost` | Origens do app, separadas por vírgula |
| `APP_ENVIRONMENT` | `homolog` | Exibida em `/actuator/info` |
| `API_PORT` | — | Opcional; porta publicada no host, padrão `8080` |

Em homologação pública, coloque a API atrás de um proxy com **HTTPS**: o app envia tokens
no cabeçalho `Authorization` e o refresh token no corpo das requisições.

## Projeto mobile e APK

Este repositório contém somente o backend. O app fica em
[LarissaFelixx/FisioTech-front](https://github.com/LarissaFelixx/FisioTech-front). No projeto mobile, aponte a URL
da API para a homologação (ex. `MOBILE_API_URL=https://api-homolog.example`) e inclua a origem do
app em `CORS_ALLOWED_ORIGINS` (`capacitor://localhost` no Android/iOS empacotado). Para
Capacitor/Angular, o fluxo genérico é:

```bash
npm ci
npm run build
npx cap sync android
cd android
./gradlew assembleDebug
```

O APK resultante fica normalmente em `android/app/build/outputs/apk/debug/app-debug.apk`. Para
entrega assinada, configure o keystore no ambiente de CI e execute `bundleRelease` ou
`assembleRelease`.

O app precisa seguir o contrato de tokens descrito em
[Autenticação JWT — Aplicativo mobile](autenticacao-jwt.md#aplicativo-mobile).

## Parâmetros externos

A URL pública, o certificado TLS, o provedor SMTP, o keystore Android e as credenciais de
publicação não estão no repositório e devem ser fornecidos por quem opera o ambiente.
