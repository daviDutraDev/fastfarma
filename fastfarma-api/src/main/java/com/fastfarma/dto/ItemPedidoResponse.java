package com.fastfarma.dto;

import com.fastfarma.model.PedidoItem;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO de saída para {@link PedidoItem}.
 * Usa a fábrica {@link #de(PedidoItem)} para conversão.
 */
@Data
@Builder
public class ItemPedidoResponse {

    private Integer produtoId;
    private String nome;
    private BigDecimal precoUnitario;

    private ItemPedidoResponse(Integer produtoId, String nome, BigDecimal precoUnitario) {
        this.produtoId = produtoId;
        this.nome = nome;
        this.precoUnitario = precoUnitario;
    }

    public static ItemPedidoResponse de(PedidoItem item) {
        return new ItemPedidoResponse(
                item.getProduto().getId(),
                item.getProduto().getNome(),
                item.getProduto().getPreco());
    }
}
