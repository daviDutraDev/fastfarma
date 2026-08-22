package com.fastfarma.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class PedidoRequest {
    @NotEmpty(message = "Lista de produtos não pode estar vazia")
    private List<Integer> idsProdutos;
}
