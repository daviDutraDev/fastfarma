# FastFarma

Sistema de gerenciamento de farmácia com **API REST (Spring Boot 3.2.5)** e
**frontend React/Vite**. Inclui autenticação JWT, BCrypt para senhas,
notificação automática por WhatsApp (Evolution API) e cobertura de testes
unitários.

```
fastfarma/
├── fastfarma-api/                   # Spring Boot 3.2.5 + JPA + Postgres
│   ├── sql/                         # schema.sql e migrations
│   └── src/
│       ├── main/java/com/fastfarma/
│       │   ├── controller/          # @RestController (Auth, Usuario, Produto, Pedido, Estoque)
│       │   ├── service/             # IAuthService, IProdutoService, IPedidoService
│       │   ├── model/               # entidades JPA (Produto, Pedido, PedidoItem, Usuario)
│       │   ├── dto/                 # ApiResponse, request e response DTOs
│       │   ├── repository/          # Spring Data JPA
│       │   ├── security/            # JwtService, JwtAuthFilter, SecurityConfig, rate limit
│       │   ├── notifications/       # NotificationService + EvolutionApiNotifier
│       │   ├── config/              # WebConfig (CORS)
│       │   └── exception/           # GlobalExceptionHandler
│       └── test/                    # testes unitarios (JUnit 5 + Mockito)
└── fastfarma-frontend/FastFarma/    # React 19 + Vite 8 + React Router 7
    └── src/
        ├── auth/                    # AuthContext (login/logout, isFuncionario)
        ├── services/api/            # httpClient (Bearer), AuthApi, Produtos, etc.
        ├── pages/                   # login, dashboard, produtos, pedidos, usuarios, estoque
        ├── components/              # header, sidebar, modais (telefone, estoque)
        └── layouts/                 # MainLayout (admin), UserLayout (cliente)
```

## Setup rápido

### 1. Backend (fastfarma-api)

```bash
cd fastfarma-api
mvn spring-boot:run
```

Por padrão, usa **H2 em memória** (PostgreSQL-compatible). Sem nenhuma
configuração adicional, a API sobe em `http://localhost:8080` e cria um
seed de admin (`admin@gmail.com` / `admin`) e três produtos.

**Para Postgres** (perfil `prod`):

```bash
psql -U postgres -c "CREATE DATABASE fastfarma_db;"
psql -U postgres -d fastfarma_db -f sql/01-schema.sql
psql -U postgres -d fastfarma_db -f sql/02-add-telefone.sql   # opcional, se ja rodou 01 sem telefone
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### 2. Frontend

```bash
cd fastfarma-frontend/FastFarma
npm install
npm run dev          # http://localhost:5173
```

Configure a URL da API em `.env` (opcional, default já aponta pra 8080):

```
VITE_API_URL=http://localhost:8080
VITE_API_TIMEOUT=15000
```

### 3. WhatsApp (Evolution API) — opcional

O backend já está integrado com a **Evolution API** e dispara mensagem
automática quando um pedido vira `PRONTO`. Para ativar:

```bash
# 1. Suba um container Docker com a Evolution API
docker run -d --name evolution \
  -p 8081:8080 \
  -e AUTHENTICATION_API_KEY=sua-chave-aqui \
  atendai/evolution-api:latest

# 2. Crie uma instancia e conecte um WhatsApp
curl -X POST http://localhost:8081/instance/create/fastfarma \
     -H "apikey: sua-chave-aqui" \
     -H "Content-Type: application/json" \
     -d '{"qrcode": true}'
# Escaneie o QR pelo endpoint /instance/connect/fastfarma

# 3. Ligue no backend
export EVOLUTION_API_URL=http://localhost:8081
export EVOLUTION_API_KEY=sua-chave-aqui
export EVOLUTION_INSTANCE=fastfarma
export FASTFARMA_WHATSAPP_ENABLED=true
mvn spring-boot:run
```

Sem essas variáveis, o envio de WhatsApp fica desativado (`enabled=false`)
e apenas loga no console — útil em dev.

## Segurança

- **Autenticação:** JWT (HMAC-SHA256) emitido no login, expiração 24h.
  Header `Authorization: Bearer <token>` em todas as chamadas autenticadas.
- **Senhas:** BCrypt (cost 10). Hash nunca trafega em claro de volta.
- **Autorização por role:**
  - `CLIENTE`: criar pedido, ver apenas os próprios pedidos, login/cadastro.
  - `FUNCIONARIO`: tudo (CRUD de produtos, usuários, estoque, mudar status).
- **Rate limit:** `/api/auth/login` 10 req/min por IP, `/api/auth/cadastro`
  5 req/min por IP. Excedeu → HTTP 429.
- **Headers OWASP:** `X-Frame-Options=deny`, `X-Content-Type-Options=nosniff`,
  HSTS, CORS restrito ao Vite.
- **CSRF:** desabilitado (API stateless).
- **Validação:** Jakarta Validation em todos os DTOs de entrada.

Para gerar um secret forte em produção:

```bash
openssl rand -base64 48    # use como FASTFARMA_JWT_SECRET
```

## Endpoints

### Públicos
- `POST /api/auth/login`          → retorna `{token, user, expiresInSeconds}`
- `POST /api/auth/cadastrar`      → cria CLIENTE

### FUNCIONARIO (Bearer)
- `GET    /api/usuarios`
- `GET    /api/usuarios/{id}`
- `DELETE /api/usuarios/{id}`     (admin id=1 protegido)
- `GET    /api/produtos`, `POST /api/produtos`, `PUT /api/produtos/{id}`, `DELETE /api/produtos/{id}`
- `PUT    /api/estoque/adicionar/{id}`
- `GET    /api/pedidos`           (lista geral)
- `GET    /api/pedidos/status/{status}`
- `PATCH  /api/pedidos/{id}/status`

### Qualquer autenticado (Bearer)
- `GET    /api/produtos/disponiveis`
- `GET    /api/produtos/esgotados`
- `GET    /api/produtos/buscar?nome=...`
- `GET    /api/pedidos/{id}`      (CLIENTE só se `pedido.criadoPor == user.nome`)
- `GET    /api/pedidos/cliente/{nome}`  (CLIENTE só se for ele mesmo)
- `POST   /api/pedidos`           (criadoPor vem do JWT, não do header)

## Testes

```bash
cd fastfarma-api
mvn test
```

Cobertura atual:

- **Entidades:** `Usuario`, `Produto`, `Pedido` — validações, regras de
  domínio, encapsulamento.
- **Serviços:** `AuthService`, `ProdutoService`, `PedidoService` — com
  Mockito, cobrindo caminhos felizes e de erro.

## Variáveis de ambiente

| Variável | Default | Descrição |
|---|---|---|
| `FASTFARMA_JWT_SECRET` | `dev-secret-troque-em-producao-32-chars-min` | Segredo HMAC (≥ 32 chars) |
| `EVOLUTION_API_URL`    | `http://localhost:8081` | URL da Evolution API |
| `EVOLUTION_API_KEY`    | (vazio) | API key da Evolution |
| `EVOLUTION_INSTANCE`   | `fastfarma` | Nome da instância |
| `FASTFARMA_WHATSAPP_ENABLED` | `false` | Liga/desliga envio real |

## Checklist de verificação manual

Antes de subir pra produção:

- [ ] `FASTFARMA_JWT_SECRET` definido com ≥ 32 chars aleatórios
- [ ] Profile `prod` ativado e Postgres acessível
- [ ] `sql/01-schema.sql` + `sql/02-add-telefone.sql` aplicados
- [ ] Senha do admin (`admin@gmail.com`) trocada
- [ ] CORS liberado apenas para origens do frontend de produção
- [ ] Evolution API rodando e instância conectada (se WhatsApp ativo)
- [ ] `mvn test` passando
- [ ] `npm run build` sem warnings críticos
- [ ] Logs estruturados (JSON) configurados no Spring
