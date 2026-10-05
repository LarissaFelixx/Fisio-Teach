# Autenticação JWT

A API aceita `Authorization: Bearer <accessToken>` nas rotas protegidas. HTTP Basic
não é mais aceito. As permissões ADMIN, PROFISSIONAL e PACIENTE e as verificações
de propriedade dos recursos permanecem nos controllers/serviços existentes.

## Contrato

### Login

`POST /auth/login`, sem Authorization, com `Content-Type: application/json`:

```json
{"email":"admin@fisiotech.com","senha":"12345678"}
```

Exemplo de resposta (tokens abreviados):

```json
{
  "accessToken": "ey...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "refreshToken": "valor-aleatorio",
  "refreshExpiresIn": 604800
}
```

Validades são em segundos. Tokens são retornados com `Cache-Control: no-store`.
Credenciais inválidas retornam 401, sem distinguir email inexistente de senha incorreta.
Dados inválidos retornam 400. Os tokens e as senhas não devem ser registrados em logs.

### Identidade e autorização

`GET /auth/me` com Bearer mantém `{id, nome, email, role}`. Os dados são carregados
da conta atual; alterar nome/email não deixa `/auth/me` desatualizado.

O JWT contém `sub` (por exemplo `PACIENTE:42`), `role`, `iss`, `aud`, `iat`, `exp`,
`jti` e `sid`. IDs iguais em tabelas diferentes não representam a mesma identidade.
Senha, email e dados clínicos não são incluídos no JWT. Assinatura RS256, emissor,
destinatário, validade, papel e vínculo com a sessão são verificados pelo servidor.

### Renovação

`POST /auth/refresh`, sem Authorization:

```json
{"refreshToken":"token-recebido-no-login-ou-na-ultima-renovacao"}
```

Retorna o mesmo formato do login. Cada renovação consome o token anterior e gera
outro. O token de renovação é aleatório (256 bits); somente seu hash SHA-256 é salvo.
A sessão expira **7 dias após o login**, por padrão; renovar não prolonga esse prazo.
O acesso dura até 15 minutos, limitado também pela validade restante da sessão.

Reutilizar um token consumido revoga a sessão inteira e retorna 401. O histórico
de hashes consumidos permite detectar reutilização. Tokens desconhecidos retornam
401 sem revogar outras sessões. A rotação usa bloqueio no banco para serializar
renovações concorrentes da mesma sessão.

### Logout e troca de senha

`POST /auth/logout`, sem Authorization, recebe `{ "refreshToken": "..." }` e retorna
204, inclusive quando repetido ou quando o token é desconhecido. Um token conhecido,
mesmo já consumido, revoga a sessão correspondente. Outras sessões permanecem ativas.

Alterar/redefinir a senha revoga todas as sessões da conta, inclusive nos endpoints
de edição administrativa. Exclusão da conta também impede o uso de tokens existentes.
Cada chamada autenticada verifica a sessão no banco e a credencial atual da conta;
portanto, logout não depende de esperar o JWT expirar. Requisições já autenticadas
e em execução no instante da revogação podem terminar.

Não há sessão HTTP/cookie de autenticação. O estado de revogação é persistido em
`auth_sessions`; por isso a validação depende do banco, mesmo com JWT assinado.

## Aplicativo mobile

1. Substituir Basic por `POST /auth/login` e Bearer nas chamadas protegidas.
2. Manter o token de acesso em memória e o de renovação no armazenamento seguro
   da plataforma (Keychain/Keystore). Não guardar a senha para renovar o login.
3. Centralizar a renovação: apenas uma chamada por vez, compartilhada pelas
   requisições que aguardam. Salvar o novo refresh token antes de reutilizá-lo.
4. Quando o acesso expirar, renovar e repetir a requisição uma única vez. Se a
   renovação retornar 401, apagar os tokens locais e solicitar novo login.
5. Não repetir cegamente uma renovação após perda da resposta: o token pode ter
   sido consumido e sua reutilização revoga a sessão. Nesse caso, refazer o login.
6. No logout, chamar o servidor e limpar os tokens locais. Se estiver offline,
   limpar localmente não revoga a sessão no servidor.

Login, refresh, logout, cadastro público e recuperação de senha (`/auth/recuperar-senha`,
`/auth/redefinir-senha`) devem sair sem um Bearer antigo. Um Bearer
inválido é rejeitado pelo filtro mesmo em uma rota pública. Usar HTTPS em produção.
O frontend fica fora deste repositório e precisa ser publicado de forma coordenada.

## Configuração

| Propriedade | Padrão |
|---|---|
| `app.jwt.issuer` | `fisiotech` |
| `app.jwt.audience` | `fisiotech-api` |
| `app.jwt.access-ttl` | `15m` |
| `app.jwt.refresh-ttl` | `7d` |

Em `dev`, um par RSA de 2048 bits é criado em memória a cada inicialização. Reiniciar
invalida os JWTs anteriores. Esse modo é adequado apenas ao desenvolvimento local.

Em `prod`, informar `JWT_PUBLIC_KEY` e `JWT_PRIVATE_KEY` como URIs de recursos,
por exemplo `file:/etc/fisiotech/jwt-public.pem` e `file:/etc/fisiotech/jwt-private.pem`.
A chave pública deve estar em PEM X.509 e a privada em PEM PKCS#8. Exemplo com OpenSSL:

```sh
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out jwt-private.pem
openssl pkey -in jwt-private.pem -pubout -out jwt-public.pem
```

Armazenar as chaves fora do repositório e restringir a leitura da privada ao processo.
Todas as instâncias devem compartilhar o par de chaves e o banco. A aplicação falha
na inicialização se as chaves obrigatórias não forem configuradas ou forem incompatíveis.
O perfil `prod` nunca utiliza a geração efêmera, mesmo se `dev` também estiver ativo.
Trocar o par invalida os JWTs existentes; não há sobreposição automática de chaves.

## Banco de produção e implantação

O projeto usa Flyway e mantém `ddl-auto=validate`. Em banco vazio, as migrações
`V1` a `V5` criam o esquema completo (lista em [Arquitetura](arquitetura.md#modelo-de-dados)).
Em banco legado não vazio e ainda sem histórico do Flyway, `baseline-on-migrate` registra
a estrutura existente como versão 1 e aplica a partir da `V2` (JWT), seguida de `V3` a `V5`.

1. Fazer backup e parar as instâncias antigas antes da alteração.
2. Executar `src/main/resources/db/manual/000-audit-emails.sql` (somente leitura). Resolver
   os emails duplicados retornados antes de prosseguir, sem excluir contas automaticamente.
3. Iniciar a nova versão, que executará `src/main/resources/db/migration/V2__seguranca_jwt.sql`.
   Ela acrescenta três tabelas e preenche o registro de emails das contas existentes.
   DDL no MySQL não é integralmente transacional: se houver erro, inspecionar as
   tabelas criadas e corrigir a causa antes de repetir qualquer etapa.
4. Configurar as chaves e iniciar com `prod`. O Hibernate valida o esquema.
5. Publicar o aplicativo que utiliza Bearer e verificar login, renovação e logout.

> **`db/manual/001-jwt-security.sql`** é a versão manual da `V2`, anterior à adoção do
> Flyway, mantida apenas como referência. **Não execute** esse script em um banco que será
> gerenciado pelo Flyway: as tabelas `auth_*` já existiriam e a `V2` falharia na subida.

O registro `auth_emails` impõe unicidade entre os três tipos de conta, inclusive em
cadastros concorrentes. Todas as alterações de email passam pelo mesmo registro
na transação da conta. A inicialização reconcilia os registros existentes e falha
se encontrar emails ambíguos. Não alterar contas diretamente no banco durante a operação.

Manter os hashes consumidos enquanto a sessão estiver válida. Para manutenção,
remover primeiro tokens de sessões expiradas e depois essas sessões, em uma transação;
não remover apenas os hashes consumidos de uma sessão ativa. Não há limpeza agendada
nesta versão.

## Swagger e verificação

O Swagger apresenta o esquema `bearerAuth`: faça login, copie apenas `accessToken`
e use **Authorize**. Os endpoints públicos não exigem esse esquema na documentação.
O H2 console só é liberado em `dev` sem `prod`.

Executar `mvnw.cmd test` (Windows) ou `./mvnw test` (Linux/macOS). A suíte inclui
testes HTTP com servidor real e H2, emissão/validação RSA, papéis, isolamento,
revogação, conflitos de email e renovação concorrente. Isso não substitui a
validação da migração SQL em uma instância MySQL nem a integração com o frontend.
