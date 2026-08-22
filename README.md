# 📋 FastFarma API — Documentação

## 🌐 URL do Projeto

```
http://localhost:8080
```

---

## 📁 Estrutura do Projeto

```
fastfarma-api/
├── sql/
│   └── 01-schema.sql          ← Script SQL do banco PostgreSQL
├── src/main/
│   ├── resources/
│   │   └── application.properties
│   └── java/com/fastfarma/
│       ├── FastFarmaApplication.java
│       ├── model/             ← Entidades JPA
│       │   ├── Usuario.java
│       │   ├── Produto.java
│       │   ├── Pedido.java
│       │   ├── PedidoItem.java
│       │   ├── TipoUsuario.java   (enum)
│       │   └── StatusPedido.java   (enum)
│       ├── repository/         ← Repositórios JPA
│       ├── dto/                ← DTOs de request/response
│       ├── service/            ← Lógica de negócio
│       └── controller/         ← REST Controllers
└── pom.xml
```

---

## 🗄️ Banco de Dados — PostgreSQL

### Tabelas

| Tabela | Descrição |
|--------|-----------|
| `usuarios` | Cadastro de usuários (clientes e funcionários) |
| `produtos` | Catálogo de produtos da farmácia |
| `pedidos` | Pedidos realizados |
| `pedido_itens` | Itens de cada pedido (relacionamento N:N) |

### Conexão (application.properties)

```
jdbc:postgresql://localhost:5432/fastfarma_db
usuário: postgres
senha:   postgres
```

---

## 🔌 Endpoints da API

### BASE URL
```
http://localhost:8080/api
```

---

### 🔐 Autenticação (`/api/auth`)

---

#### `POST /api/auth/login`
Realiza login de usuário.

**Body (JSON):**
```json
{
  "email": "admin@gmail.com",
  "senha": "admin"
}
```

| Campo | Tipo | Obrigatório | Descrição |
|-------|------|:------------:|-----------|
| `email` | String | ✅ | Email do usuário |
| `senha` | String | ✅ | Senha do usuário |

**Resposta (200 OK):**
```json
{
  "sucesso": true,
  "mensagem": "Login realizado com sucesso",
  "dados": {
    "id": 1,
    "nome": "admin",
    "email": "admin@gmail.com",
    "tipo": "FUNCIONARIO",
    "mensagem": "Login realizado com sucesso!"
  }
}
```

**Resposta (401 Unauthorized):**
```json
{
  "sucesso": false,
  "mensagem": "Email ou senha incorretos"
}
```

---

#### `POST /api/auth/cadastrar`
Cadastra um novo cliente.

**Body (JSON):**
```json
{
  "nome": "João Silva",
  "email": "joao@email.com",
  "senha": "123456"
}
```

| Campo | Tipo | Obrigatório | Descrição |
|-------|------|:------------:|-----------|
| `nome` | String | ✅ | Nome completo (2-100 chars) |
| `email` | String | ✅ | Email válido e único |
| `senha` | String | ✅ | Senha (mín. 4 caracteres) |

**Resposta (201 Created):**
```json
{
  "sucesso": true,
  "mensagem": "Usuário cadastrado com sucesso",
  "dados": {
    "id": 2,
    "nome": "João Silva",
    "email": "joao@email.com",
    "tipo": "CLIENTE",
    "criadoEm": "2026-08-19T14:30:00"
  }
}
```

---

### 👤 Usuários (`/api/usuarios`)

---

#### `GET /api/usuarios/{id}`
Busca um usuário pelo ID.

**Resposta (200 OK):**
```json
{
  "sucesso": true,
  "mensagem": "Usuário encontrado",
  "dados": {
    "id": 1,
    "nome": "admin",
    "email": "admin@gmail.com",
    "tipo": "FUNCIONARIO",
    "criadoEm": "2026-08-19T10:00:00"
  }
}
```

---

### 💊 Produtos (`/api/produtos`)

---

#### `GET /api/produtos`
Lista todos os produtos.

**Resposta (200 OK):**
```json
{
  "sucesso": true,
  "mensagem": "Lista de produtos",
  "dados": [
    {
      "id": 1,
      "nome": "Dipirona",
      "preco": 10.50,
      "estoque": 20,
      "situacao": "Disponivel",
      "criadoEm": "2026-08-19T10:00:00",
      "atualizadoEm": "2026-08-19T10:00:00"
    }
  ]
}
```

---

#### `GET /api/produtos/{id}`
Busca um produto específico.

**Resposta (200 OK):** mesmo formato do listar.

**Resposta (404 Not Found):**
```json
{
  "sucesso": false,
  "mensagem": "Produto não encontrado"
}
```

---

#### `GET /api/produtos/buscar?nome={nome}`
Busca produtos pelo nome (busca parcial, case-insensitive).

**Exemplo:** `GET /api/produtos/buscar?nome=dip`

---

#### `GET /api/produtos/disponiveis`
Lista apenas produtos com estoque > 0.

---

#### `GET /api/produtos/esgotados`
Lista apenas produtos com estoque = 0.

---

#### `POST /api/produtos`
Cria um novo produto.

**Body (JSON):**
```json
{
  "nome": "Ibuprofeno",
  "preco": 12.50,
  "estoque": 30
}
```

| Campo | Tipo | Obrigatório | Descrição |
|-------|------|:------------:|-----------|
| `nome` | String | ✅ | Nome do produto |
| `preco` | Number | ✅ | Preço (deve ser > 0) |
| `estoque` | Integer | ✅ | Quantidade em estoque (≥ 0) |

**Resposta (201 Created):**
```json
{
  "sucesso": true,
  "mensagem": "Produto criado com sucesso",
  "dados": {
    "id": 4,
    "nome": "Ibuprofeno",
    "preco": 12.50,
    "estoque": 30,
    "situacao": "Disponivel"
  }
}
```

---

#### `PUT /api/produtos/{id}`
Atualiza todos os dados de um produto.

**Body (JSON):**
```json
{
  "nome": "Ibuprofeno 600mg",
  "preco": 14.90,
  "estoque": 50
}
```

| Campo | Tipo | Obrigatório | Descrição |
|-------|------|:------------:|-----------|
| `nome` | String | ✅ | Nome atualizado |
| `preco` | Number | ✅ | Novo preço |
| `estoque` | Integer | ✅ | Nova quantidade |

**Resposta (200 OK):**
```json
{
  "sucesso": true,
  "mensagem": "Produto atualizado com sucesso",
  "dados": { ... }
}
```

---

#### `DELETE /api/produtos/{id}`
Exclui um produto pelo ID.

**Resposta (200 OK):**
```json
{
  "sucesso": true,
  "mensagem": "Produto excluído com sucesso"
}
```

---

### 📦 Estoque (`/api/estoque`)

---

#### `PUT /api/estoque/adicionar/{id}`
Adiciona quantidade ao estoque de um produto.

**Body (JSON):**
```json
{
  "quantidade": 10
}
```

| Campo | Tipo | Obrigatório | Descrição |
|-------|------|:------------:|-----------|
| `quantidade` | Integer | ✅ | Quantidade a adicionar (mín. 1) |

**Resposta (200 OK):**
```json
{
  "sucesso": true,
  "mensagem": "Estoque adicionado com sucesso",
  "dados": {
    "id": 1,
    "nome": "Dipirona",
    "preco": 10.50,
    "estoque": 30,
    "situacao": "Disponivel"
  }
}
```

---

### 🛒 Pedidos (`/api/pedidos`)

---

#### `GET /api/pedidos`
Lista todos os pedidos (ordenados do mais recente ao mais antigo).

**Resposta (200 OK):**
```json
{
  "sucesso": true,
  "mensagem": "Lista de pedidos",
  "dados": [
    {
      "id": 1,
      "codigoVerificacao": 1234,
      "criadoPor": "João Silva",
      "status": "PENDENTE",
      "itens": [
        {
          "produtoId": 1,
          "nome": "Dipirona",
          "precoUnitario": 10.50
        }
      ],
      "valorTotal": 10.50,
      "criadoEm": "2026-08-19T14:30:00",
      "atualizadoEm": "2026-08-19T14:30:00"
    }
  ]
}
```

---

#### `GET /api/pedidos/{id}`
Busca um pedido específico pelo ID.

---

#### `GET /api/pedidos/cliente/{nome}`
Lista todos os pedidos de um cliente pelo nome.

**Exemplo:** `GET /api/pedidos/cliente/João%20Silva`

---

#### `GET /api/pedidos/status/{status}`
Lista pedidos filtrados por status.

**Status válidos:** `PENDENTE`, `APROVADO`, `REJEITADO`, `PRONTO`

**Exemplo:** `GET /api/pedidos/status/PENDENTE`

---

#### `POST /api/pedidos`
Cria um novo pedido. **Requer header `X-Usuario-Nome`.**

**Header obrigatório:**
```
X-Usuario-Nome: João Silva
```

**Body (JSON):**
```json
{
  "idsProdutos": [1, 2, 3]
}
```

| Campo | Tipo | Obrigatório | Descrição |
|-------|------|:------------:|-----------|
| `idsProdutos` | Array\<Integer\> | ✅ | Lista de IDs dos produtos |

**Resposta (201 Created):**
```json
{
  "sucesso": true,
  "mensagem": "Pedido criado com sucesso",
  "dados": {
    "id": 5,
    "codigoVerificacao": 4821,
    "criadoPor": "João Silva",
    "status": "PENDENTE",
    "itens": [
      { "produtoId": 1, "nome": "Dipirona", "precoUnitario": 10.50 },
      { "produtoId": 2, "nome": "Paracetamol", "precoUnitario": 8.00 }
    ],
    "valorTotal": 18.50,
    "criadoEm": "2026-08-19T15:00:00",
    "atualizadoEm": "2026-08-19T15:00:00"
  }
}
```

> ⚠️ Ao criar um pedido, o estoque dos produtos é automaticamente decrementado.

---

#### `PATCH /api/pedidos/{id}/status`
Atualiza o status de um pedido (funcionário only).

**Body (JSON):**
```json
{
  "status": "APROVADO"
}
```

| Campo | Tipo | Obrigatório | Descrição |
|-------|------|:------------:|-----------|
| `status` | String | ✅ | Novo status (PENDENTE, APROVADO, REJEITADO, PRONTO) |

**Status válidos:** `PENDENTE`, `APROVADO`, `REJEITADO`, `PRONTO`

**Resposta (200 OK):**
```json
{
  "sucesso": true,
  "mensagem": "Status atualizado para APROVADO",
  "dados": {
    "id": 5,
    "status": "APROVADO",
    ...
  }
}
```

> ⚠️ Se o status for `REJEITADO`, o estoque dos produtos é automaticamente devolvido.

---

## 🚀 Como Executar

### 1. Configurar o banco
```bash
# Acessar PostgreSQL e criar o banco
psql -U postgres -c "CREATE DATABASE fastfarma_db;"
psql -U postgres -d fastfarma_db -f sql/01-schema.sql
```

### 2. Configurar credenciais
Edite `fastfarma-api/src/main/resources/application.properties` com seu usuário e senha do PostgreSQL.

### 3. Compilar e rodar
```bash
cd fastfarma-api
mvn spring-boot:run
```

### 4. Testar
Acesse: `http://localhost:8080/api/produtos`

---

## 📊 Resumo dos Endpoints

| Método | Endpoint | Descrição | Body |
|--------|----------|-----------|------|
| POST | `/api/auth/login` | Login | `{email, senha}` |
| POST | `/api/auth/cadastrar` | Cadastro | `{nome, email, senha}` |
| GET | `/api/usuarios/{id}` | Buscar usuário | — |
| GET | `/api/produtos` | Listar todos | — |
| GET | `/api/produtos/{id}` | Buscar produto | — |
| GET | `/api/produtos/buscar?nome=` | Buscar por nome | — |
| GET | `/api/produtos/disponiveis` | Disponíveis | — |
| GET | `/api/produtos/esgotados` | Esgotados | — |
| POST | `/api/produtos` | Criar produto | `{nome, preco, estoque}` |
| PUT | `/api/produtos/{id}` | Atualizar produto | `{nome, preco, estoque}` |
| DELETE | `/api/produtos/{id}` | Excluir produto | — |
| PUT | `/api/estoque/adicionar/{id}` | Adicionar estoque | `{quantidade}` |
| GET | `/api/pedidos` | Listar pedidos | — |
| GET | `/api/pedidos/{id}` | Buscar pedido | — |
| GET | `/api/pedidos/cliente/{nome}` | Pedidos do cliente | — |
| GET | `/api/pedidos/status/{status}` | Filtrar por status | — |
| POST | `/api/pedidos` | Criar pedido | `{idsProdutos}` + header |
| PATCH | `/api/pedidos/{id}/status` | Atualizar status | `{status}` |
