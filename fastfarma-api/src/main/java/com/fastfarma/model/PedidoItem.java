package com.fastfarma.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pedido_itens")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
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
        this.pedido = pedido;
        this.produto = produto;
    }
}
