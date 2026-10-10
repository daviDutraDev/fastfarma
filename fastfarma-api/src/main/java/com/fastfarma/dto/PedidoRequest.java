package com.fastfarma.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class PedidoRequest {

    @Valid
    @NotEmpty(message = "Lista de produtos não pode estar vazia")
    private List<Item> itens;

    /** Um produto do pedido com a quantidade desejada. */
    @Data
    public static class Item {
        @NotNull(message = "Produto é obrigatório")
        private Integer produtoId;

        @NotNull(message = "Quantidade é obrigatória")
        @Min(value = 1, message = "Quantidade deve ser pelo menos 1")
        private Integer quantidade;
    }
}