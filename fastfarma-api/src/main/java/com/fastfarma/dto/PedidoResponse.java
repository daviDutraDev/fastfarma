package com.fastfarma.dto;

import com.fastfarma.model.StatusPedido;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder
public class PedidoResponse {
    private Integer id;
    private Integer codigoVerificacao;
    private String criadoPor;
    private StatusPedido status;
    private List<ItemPedidoResponse> itens;
    private BigDecimal valorTotal;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
}
