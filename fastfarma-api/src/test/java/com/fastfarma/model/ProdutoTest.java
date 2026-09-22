package com.fastfarma.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProdutoTest {

    @Test
    void construtor_deve_aceitar_valores_validos() {
        Produto p = new Produto("Dipirona", new BigDecimal("10.50"), 10);
        assertEquals("Dipirona", p.getNome());
        assertEquals(new BigDecimal("10.50"), p.getPreco());
        assertEquals(10, p.getEstoque());
        assertEquals("Disponivel", p.getSituacao());
    }

    @Test
    void construtor_com_categoria_deve_normalizar() {
        Produto p = new Produto("Vitamina C", new BigDecimal("15"), 5, "  Suplemento  ");
        assertEquals("Suplemento", p.getCategoria());
    }

    @Test
    void categoria_vazia_deve_ser_normalizada_para_null() {
        Produto p = new Produto("X", new BigDecimal("1.00"), 1, "");
        assertNull(p.getCategoria());
    }

    @Test
    void categoria_longa_demais_deve_lancar() {
        String longa = "a".repeat(51);
        Produto p = new Produto("X", new BigDecimal("1.00"), 1);
        assertThrows(IllegalArgumentException.class, () -> p.setCategoria(longa));
    }

    @Test
    void nome_vazio_deve_lancar() {
        Produto p = new Produto("Ok", new BigDecimal("1.00"), 1);
        assertThrows(IllegalArgumentException.class, () -> p.setNome(""));
    }

    @Test
    void preco_zero_ou_negativo_deve_lancar() {
        Produto p = new Produto("X", new BigDecimal("1.00"), 1);
        assertThrows(IllegalArgumentException.class, () -> p.setPreco(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> p.setPreco(new BigDecimal("-1")));
    }

    @Test
    void estoque_negativo_deve_lancar() {
        Produto p = new Produto("X", new BigDecimal("1.00"), 1);
        assertThrows(IllegalArgumentException.class, () -> p.setEstoque(-1));
    }

    @Test
    void reduzir_estoque_deve_validar_quantidade_e_saldo() {
        Produto p = new Produto("X", new BigDecimal("1.00"), 5);
        p.reduzirEstoque(2);
        assertEquals(3, p.getEstoque());

        assertThrows(IllegalArgumentException.class, () -> p.reduzirEstoque(0));
        assertThrows(IllegalStateException.class, () -> p.reduzirEstoque(99));
    }

    @Test
    void adicionar_estoque_deve_somar() {
        Produto p = new Produto("X", new BigDecimal("1.00"), 1);
        p.adicionarEstoque(4);
        assertEquals(5, p.getEstoque());
        assertThrows(IllegalArgumentException.class, () -> p.adicionarEstoque(0));
    }

    @Test
    void situacao_deve_refletir_estoque() {
        Produto p = new Produto("X", new BigDecimal("1.00"), 0);
        assertEquals("Esgotado", p.getSituacao());
        p.adicionarEstoque(3);
        assertEquals("Disponivel", p.getSituacao());
    }

    @Test
    void estoque_baixo_deve_usar_constante() {
        Produto p = new Produto("X", new BigDecimal("1.00"), Produto.ESTOQUE_MINIMO_ALERTA);
        assertFalse(p.estoqueBaixo(), "No limite ainda nao e baixo");
        p.reduzirEstoque(1);
        assertTrue(p.estoqueBaixo(), "Abaixo do limite e baixo");
    }
}
