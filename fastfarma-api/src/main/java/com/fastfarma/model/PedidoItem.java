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

    public PedidoItem(Pedido pedido, Produto produto) {
        setPedido(pedido);
        setProduto(produto);
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

    /**
     * Subtotal deste item no pedido. Hoje a regra é só o preço do
     * produto (sem quantidade), mas fica encapsulada aqui para
     * acomodar mudanças (ex.: campo quantidade) sem mexer no service.
     */
    public BigDecimal getSubtotal() {
        return produto == null || produto.getPreco() == null
                ? BigDecimal.ZERO
                : produto.getPreco();
    }

    @Override
    public String toString() {
        return "PedidoItem{id=" + id
                + ", produto=" + (produto == null ? "null" : produto.getNome())
                + ", subtotal=" + getSubtotal() + "}";
    }
}
