package com.fastfarma.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PedidoTest {

    private Produto produto(String nome, String preco, int estoque) {
        return new Produto(nome, new BigDecimal(preco), estoque);
    }

    @Test
    void novo_pedido_deve_ter_status_pendente_e_codigo_gerado() {
        Pedido p = new Pedido("cliente");
        assertEquals(StatusPedido.PENDENTE, p.getStatus());
        assertNotNull(p.getCodigoVerificacao());
        assertTrue(p.getCodigoVerificacao() >= Pedido.CODIGO_VERIFICACAO_MIN);
        assertTrue(p.getCodigoVerificacao() <= Pedido.CODIGO_VERIFICACAO_MAX);
        assertTrue(p.isPendente());
    }

    @Test
    void adicionar_item_deve_somar_valor_total() {
        Pedido p = new Pedido("cliente");
        p.adicionarItem(produto("A", "10.00", 5));
        p.adicionarItem(produto("B", "20.00", 5));
        p.adicionarItem(produto("A", "10.00", 5));   // duplicado: deve falhar
        assertEquals(2, p.getItens().size());
        assertEquals(new BigDecimal("30.00"), p.getValorTotal());
    }

    @Test
    void adicionar_item_duplicado_deve_lancar() {
        Pedido p = new Pedido("cliente");
        Produto a = produto("A", "10.00", 5);
        p.adicionarItem(a);
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> p.adicionarItem(a));
        assertTrue(ex.getMessage().contains("ja esta no pedido"));
    }

    @Test
    void adicionar_item_nulo_deve_lancar() {
        Pedido p = new Pedido("cliente");
        assertThrows(IllegalArgumentException.class, () -> p.adicionarItem(null));
    }

    @Test
    void transicoes_de_status_devem_funcionar() {
        Pedido p = new Pedido("cliente");
        p.aprovar();
        assertEquals(StatusPedido.APROVADO, p.getStatus());
        assertTrue(p.isAprovado());

        p.marcarComoPronto();
        assertEquals(StatusPedido.PRONTO, p.getStatus());
        assertTrue(p.isPronto());

        p.rejeitar();
        assertEquals(StatusPedido.REJEITADO, p.getStatus());
        assertTrue(p.isRejeitado());
    }

    @Test
    void getItens_deve_ser_imutavel() {
        Pedido p = new Pedido("cliente");
        p.adicionarItem(produto("A", "10.00", 5));
        assertThrows(UnsupportedOperationException.class,
                () -> p.getItens().clear());
    }
}
