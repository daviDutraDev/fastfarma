package com.fastfarma.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder
public class ProdutoResponse {
    private Integer id;
    private String nome;
    private BigDecimal preco;
    private Integer estoque;
    private String situacao; // "Disponivel" ou "Esgotado"
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
}
