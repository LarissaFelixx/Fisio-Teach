# Homologação da API e integração do APK

## API

1. Copie `.env.homolog.example` para `.env.homolog` e preencha os segredos fora do Git.
2. Disponibilize as chaves RSA nos caminhos definidos por `JWT_PRIVATE_KEY` e `JWT_PUBLIC_KEY`.
3. Execute `docker compose -f compose.homolog.yml up --build -d`.
4. Confirme `GET /actuator/health/readiness` com estado `UP` e valide o Swagger em `/swagger-ui.html`.

O perfil `homolog` executa Flyway na inicialização, valida o esquema com Hibernate e recebe banco, JWT, SMTP, CORS e administrador por variáveis de ambiente.

## Projeto mobile e APK

Este repositório contém somente o backend. No projeto mobile, defina `MOBILE_API_URL=https://api-homolog.example` e gere o pacote Android pelo processo da tecnologia usada pelo app. Para Capacitor/Angular, o fluxo genérico é:

```bash
npm ci
npm run build
npx cap sync android
cd android
./gradlew assembleDebug
```

O APK resultante fica normalmente em `android/app/build/outputs/apk/debug/app-debug.apk`. Para entrega assinada, configure o keystore no ambiente de CI e execute `bundleRelease` ou `assembleRelease`. A URL real, o repositório mobile, o certificado e as credenciais de publicação permanecem parâmetros externos.
