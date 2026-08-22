package com.fastfarma.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data @Builder
public class ItemPedidoResponse {
    private Integer produtoId;
    private String nome;
    private BigDecimal precoUnitario;
}
