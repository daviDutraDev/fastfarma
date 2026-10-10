package com.fastfarma.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Item de um {@link Pedido} (relacionamento N:N entre pedido e produto).
 *
 * <p>Encapsulamento aplicado: campos {@code private} e invariantes
 * verificadas nos setters (pedido e produto não podem ser nulos).</p>
 */
@Entity
@Table(name = "pedido_itens")
@Getter
@Setter
@NoArgsConstructor
public class PedidoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    /** Unidades deste produto no pedido (minimo 1). */
    @Column(nullable = false)
    private Integer quantidade = 1;

    public PedidoItem(Pedido pedido, Produto produto) {
        this(pedido, produto, 1);
    }

    public PedidoItem(Pedido pedido, Produto produto, int quantidade) {
        setPedido(pedido);
        setProduto(produto);
        setQuantidade(quantidade);
    }

    public void setPedido(Pedido pedido) {
        if (pedido == null) {
            throw new IllegalArgumentException("Pedido do item é obrigatório");
        }
        this.pedido = pedido;
    }

    public void setProduto(Produto produto) {
        if (produto == null) {
            throw new IllegalArgumentException("Produto do item é obrigatório");
        }
        this.produto = produto;
    }

    public void setQuantidade(Integer quantidade) {
        if (quantidade == null || quantidade < 1) {
            throw new IllegalArgumentException("Quantidade do item deve ser pelo menos 1");
        }
        this.quantidade = quantidade;
    }

    /** Subtotal deste item: preço unitário x quantidade. */
    public BigDecimal getSubtotal() {
        return produto == null || produto.getPreco() == null
                ? BigDecimal.ZERO
                : produto.getPreco().multiply(BigDecimal.valueOf(quantidade));
    }

    @Override
    public String toString() {
        return "PedidoItem{id=" + id
                + ", produto=" + (produto == null ? "null" : produto.getNome())
                + ", quantidade=" + quantidade
                + ", subtotal=" + getSubtotal() + "}";
    }
}