package com.fastfarma.dto;

import com.fastfarma.model.Produto;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de saída para {@link Produto}.
 *
 * <p>Encapsulamento: campos {@code private} e imutabilidade do objeto
 * via {@code @Builder} + construtor gerado. A fábrica
 * {@link #de(Produto)} centraliza a conversão entidade → DTO.</p>
 */
@Data
@Builder
public class ProdutoResponse {

    private Integer id;
    private String nome;
    private BigDecimal preco;
    private Integer estoque;
    private String situacao; // "Disponivel" ou "Esgotado"
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    /** Construtor privado — uso exclusivo da fábrica {@link #de(Produto)}. */
    private ProdutoResponse(Integer id, String nome, BigDecimal preco, Integer estoque,
                            String situacao, LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
        this.id = id;
        this.nome = nome;
        this.preco = preco;
        this.estoque = estoque;
        this.situacao = situacao;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    /** Converte a entidade de domínio em DTO de resposta. */
    public static ProdutoResponse de(Produto p) {
        return new ProdutoResponse(
                p.getId(),
                p.getNome(),
                p.getPreco(),
                p.getEstoque(),
                p.getSituacao(),
                p.getCriadoEm(),
                p.getAtualizadoEm());
    }
}
