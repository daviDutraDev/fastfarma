package com.fastfarma.dto;

import com.fastfarma.model.Pedido;
import com.fastfarma.model.StatusPedido;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO de saída para {@link Pedido}.
 * Usa a fábrica {@link #de(Pedido)} para conversão.
 */
@Data
@Builder
public class PedidoResponse {

    private Integer id;
    private Integer codigoVerificacao;
    private String criadoPor;
    private StatusPedido status;
    private List<ItemPedidoResponse> itens;
    private BigDecimal valorTotal;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    private PedidoResponse(Integer id, Integer codigoVerificacao, String criadoPor,
                            StatusPedido status, List<ItemPedidoResponse> itens,
                            BigDecimal valorTotal, LocalDateTime criadoEm,
                            LocalDateTime atualizadoEm) {
        this.id = id;
        this.codigoVerificacao = codigoVerificacao;
        this.criadoPor = criadoPor;
        this.status = status;
        this.itens = itens;
        this.valorTotal = valorTotal;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public static PedidoResponse de(Pedido p) {
        List<ItemPedidoResponse> itens = p.getItens().stream()
                .map(ItemPedidoResponse::de)
                .collect(Collectors.toList());
        return new PedidoResponse(
                p.getId(),
                p.getCodigoVerificacao(),
                p.getCriadoPor(),
                p.getStatus(),
                itens,
                p.getValorTotal(),
                p.getCriadoEm(),
                p.getAtualizadoEm());
    }
}
