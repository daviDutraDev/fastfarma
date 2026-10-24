package com.fastfarma.dto;

import com.fastfarma.model.StatusPedido;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Resultados calculados uma única vez para cada documento. */
public record RelatorioResumo(
        LocalDate inicio, LocalDate fim, boolean todoPeriodo, long dias, LocalDateTime geradoEm,
        long recebidos, long validos, long semData, long clientes, BigDecimal valor, BigDecimal media,
        Map<StatusPedido, Long> status, List<RankingProduto> produtosPorQuantidade,
        List<RankingProduto> produtosPorValor, List<RankingCliente> rankingClientes,
        List<Evolucao> evolucao, Comparacao comparacao, int coberturaDesejada, List<Reposicao> estoque) {

    public record RankingProduto(Integer id, String nome, long unidades, BigDecimal valor) {}
    public record RankingCliente(String nome, long pedidos, BigDecimal valor) {}
    public record Evolucao(String periodo, long pedidos, BigDecimal valor) {}
    public record Comparacao(LocalDate inicio, LocalDate fim, long pedidos, BigDecimal valor) {}
    public record Reposicao(Integer id, String nome, long estoque, long unidades,
            BigDecimal mediaDiaria, BigDecimal cobertura, long sugestao, String prioridade) {}
}
