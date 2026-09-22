-- ================================================
-- Migration: adiciona coluna telefone na tabela usuarios
-- ================================================
-- Idempotente: pode rodar mais de uma vez sem erro.
-- ================================================

ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS telefone VARCHAR(20);
