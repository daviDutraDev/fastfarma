-- ================================================
-- FastFarma - Banco de Dados - Schema SQL
-- ================================================
-- Banco: fastfarma_db
-- Motor: PostgreSQL 14+
-- ================================================
-- Antes de rodar este script, crie o banco com:
--   psql -U postgres -c "CREATE DATABASE fastfarma_db;"
-- E depois execute este arquivo:
--   psql -U postgres -d fastfarma_db -f sql/01-schema.sql
-- ================================================

-- ================================================
-- TABELA: usuarios
-- ================================================
CREATE TABLE IF NOT EXISTS usuarios (
    id            SERIAL PRIMARY KEY,
    nome          VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    senha         VARCHAR(255) NOT NULL,
    tipo          VARCHAR(20)  NOT NULL DEFAULT 'CLIENTE'
                    CHECK (tipo IN ('CLIENTE', 'FUNCIONARIO')),
    criado_em     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- ================================================
-- TABELA: produtos
-- ================================================
CREATE TABLE IF NOT EXISTS produtos (
    id            SERIAL PRIMARY KEY,
    nome          VARCHAR(200) NOT NULL,
    preco         DECIMAL(10,2) NOT NULL,
    estoque       INTEGER       NOT NULL DEFAULT 0,
    criado_em     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- ================================================
-- TABELA: pedidos
-- ================================================
CREATE TABLE IF NOT EXISTS pedidos (
    id                  SERIAL PRIMARY KEY,
    codigo_verificacao  INTEGER     NOT NULL,
    criado_por         VARCHAR(100) NOT NULL,
    status             VARCHAR(20)  NOT NULL DEFAULT 'PENDENTE'
                         CHECK (status IN ('PENDENTE', 'APROVADO', 'REJEITADO', 'PRONTO')),
    criado_em          TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    atualizado_em      TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

-- ================================================
-- TABELA: pedido_itens  (relacionamento N:N)
-- ================================================
CREATE TABLE IF NOT EXISTS pedido_itens (
    id          SERIAL PRIMARY KEY,
    pedido_id   INTEGER NOT NULL REFERENCES pedidos(id) ON DELETE CASCADE,
    produto_id  INTEGER NOT NULL REFERENCES produtos(id) ON DELETE CASCADE,
    UNIQUE (pedido_id, produto_id)
);

-- ================================================
-- ÍNDICES para performance
-- ================================================
CREATE INDEX IF NOT EXISTS idx_pedidos_status    ON pedidos(status);
CREATE INDEX IF NOT EXISTS idx_pedidos_criado_por ON pedidos(criado_por);
CREATE INDEX IF NOT EXISTS idx_pedido_itens_pedido  ON pedido_itens(pedido_id);
CREATE INDEX IF NOT EXISTS idx_pedido_itens_produto ON pedido_itens(produto_id);

-- ================================================
-- DADOS INICIAIS
-- ================================================

-- Admin padrão
INSERT INTO usuarios (id, nome, email, senha, tipo) VALUES
(1, 'admin', 'admin@gmail.com', 'admin', 'FUNCIONARIO')
ON CONFLICT (id) DO NOTHING;

-- Produtos iniciais
INSERT INTO produtos (id, nome, preco, estoque) VALUES
(1, 'Dipirona',    10.50, 20),
(2, 'Paracetamol',  8.00, 20),
(3, 'Vitamina C',  15.00, 20)
ON CONFLICT (id) DO NOTHING;
