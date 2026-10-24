package com.fastfarma.service;

import com.fastfarma.dto.RelatorioResumo;
import com.fastfarma.model.*;
import com.fastfarma.repository.PedidoRepository;
import com.fastfarma.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

class RelatorioServiceTest {
    private PedidoRepository pedidos;
    private ProdutoRepository produtos;
    private RelatorioService service;
    private final List<Pedido> registros = new ArrayList<>();
    private final List<Produto> catalogo = new ArrayList<>();
    private final LocalDate inicio = LocalDate.of(2026, 10, 1);
    private final LocalDate fim = LocalDate.of(2026, 10, 10);

    @BeforeEach void preparar() {
        pedidos = mock(PedidoRepository.class);
        produtos = mock(ProdutoRepository.class);
        service = new RelatorioService(pedidos, produtos, Clock.fixed(Instant.parse("2026-10-21T15:00:00Z"), ZoneId.of("America/Sao_Paulo")));
        when(pedidos.findAll()).thenAnswer(i -> registros);
        when(produtos.findAll()).thenAnswer(i -> catalogo);
        when(pedidos.findByCriadoEmGreaterThanEqualAndCriadoEmLessThan(any(), any())).thenAnswer(i -> {
            LocalDateTime de = i.getArgument(0), ate = i.getArgument(1);
            return registros.stream().filter(p -> p.getCriadoEm() != null && !p.getCriadoEm().isBefore(de) && p.getCriadoEm().isBefore(ate)).toList();
        });
    }

    @Test void deveExcluirRejeitadosDosValoresRankingsEDemanda() {
        Produto a = produto(1, "A", "10.00", 2), b = produto(2, "B", "4.00", 0);
        produto(3, "Sem procura", "5.00", 100);
        pedido("Ana", "2026-10-01T00:00:00", StatusPedido.PENDENTE, a, 2);
        Pedido segundo = pedido("Bruno", "2026-10-10T23:59:59.999999999", StatusPedido.PRONTO, a, 1);
        segundo.adicionarItem(b, 3);
        pedido("Rejeitado", "2026-10-05T12:00:00", StatusPedido.REJEITADO, b, 100);
        pedido("Anterior", "2026-09-30T23:59:59", StatusPedido.PRONTO, a, 1);
        pedido("Depois", "2026-10-11T00:00:00", StatusPedido.PRONTO, a, 100);

        RelatorioResumo r = service.resumir(inicio, fim, 30);
        assertEquals(3, r.recebidos()); assertEquals(2, r.validos()); assertEquals(2, r.clientes());
        assertEquals(new BigDecimal("42.00"), r.valor()); assertEquals(new BigDecimal("21.00"), r.media());
        assertEquals(1L, r.status().get(StatusPedido.REJEITADO));
        assertEquals(List.of("A", "B"), r.produtosPorQuantidade().stream().map(RelatorioResumo.RankingProduto::nome).toList());
        assertEquals(3, r.produtosPorQuantidade().getFirst().unidades());
        assertEquals(1, r.comparacao().pedidos());
        assertEquals(LocalDate.of(2026, 9, 21), r.comparacao().inicio());
        assertEquals(LocalDate.of(2026, 9, 30), r.comparacao().fim());
        assertEquals(10, r.evolucao().size());
        assertEquals(2, r.evolucao().stream().mapToLong(RelatorioResumo.Evolucao::pedidos).sum());
        assertEquals(0, r.evolucao().get(1).pedidos());
        var ra = r.estoque().stream().filter(p -> p.id() == 1).findFirst().orElseThrow();
        assertEquals(new BigDecimal("0.30"), ra.mediaDiaria());
        assertEquals(new BigDecimal("6.7"), ra.cobertura());
        assertEquals(7, ra.sugestao()); assertEquals("Alta", ra.prioridade());
        var rb = r.estoque().stream().filter(p -> p.id() == 2).findFirst().orElseThrow();
        assertEquals(9, rb.sugestao());
        var rc = r.estoque().stream().filter(p -> p.id() == 3).findFirst().orElseThrow();
        assertNull(rc.cobertura()); assertEquals(0, rc.sugestao()); assertEquals("Sem demanda", rc.prioridade());
    }

    @Test void deveArredondarReposicaoSomenteAposUsarRazaoExata() {
        Produto p = produto(1, "Pouca procura", "1.00", 0);
        pedido("Ana", "2026-09-22T10:00:00", StatusPedido.APROVADO, p, 1);
        var r = service.resumir(LocalDate.of(2026, 9, 22), LocalDate.of(2026, 10, 21), 30);
        assertEquals(30, r.dias()); assertEquals(1, r.estoque().getFirst().sugestao());
    }

    @Test void todoPeriodoUsaDiasAteHojeESemComparacao() {
        Produto p = produto(1, "A", "10.00", 100);
        pedido("Ana", "2026-10-01T10:00:00", StatusPedido.APROVADO, p, 21);
        var r = service.resumir(null, null, 30);
        assertTrue(r.todoPeriodo()); assertEquals(21, r.dias()); assertNull(r.comparacao());
        assertEquals(new BigDecimal("1.00"), r.estoque().getFirst().mediaDiaria());
        assertEquals(0, r.estoque().getFirst().sugestao());
        verify(pedidos, never()).findByCriadoEmGreaterThanEqualAndCriadoEmLessThan(any(), any());
    }

    @Test void semDataEntraNoResumoMasNaoNaDemandaNemEvolucao() {
        Produto p = produto(1, "A", "10.00", 0);
        pedido("Ana", null, StatusPedido.PRONTO, p, 10);
        var r = service.resumir(null, null, 30);
        assertEquals(1, r.semData()); assertEquals(1, r.validos());
        assertEquals(0, r.estoque().getFirst().unidades());
        assertNull(r.estoque().getFirst().cobertura());
        assertEquals(0, r.evolucao().stream().mapToLong(RelatorioResumo.Evolucao::pedidos).sum());
    }

    @Test void baseVaziaNaoDeveGerarDivisaoPorZeroOuMediaFicticia() {
        var r = service.resumir(inicio, fim, 30);
        assertEquals(0, r.validos()); assertNull(r.media()); assertEquals(BigDecimal.ZERO, r.valor());
        assertTrue(r.rankingClientes().isEmpty()); assertTrue(r.estoque().isEmpty());
    }

    @Test void prioridadeDeveUsarCoberturaExataMesmoQuandoExibicaoArredondaParaSeteDias() {
        Produto p = produto(1, "A", "10.00", 16);
        pedido("Ana", "2026-10-01T10:00:00", StatusPedido.APROVADO, p, 23);
        var item = service.resumir(inicio, fim, 30).estoque().getFirst();
        assertEquals(new BigDecimal("7.0"), item.cobertura());
        assertEquals("Alta", item.prioridade());
    }

    @Test void rankingDeClientesConsideraNumeroDePedidosERankingDeValorEIndependenteDeQuantidade() {
        Produto barato = produto(1, "Barato", "1.00", 10), caro = produto(2, "Caro", "100.00", 10);
        pedido("Ana", "2026-10-01T10:00:00", StatusPedido.PENDENTE, barato, 5);
        pedido("Ana", "2026-10-02T10:00:00", StatusPedido.PRONTO, barato, 5);
        pedido("Bruno", "2026-10-03T10:00:00", StatusPedido.APROVADO, caro, 1);
        var r = service.resumir(inicio, fim, 30);
        assertEquals("Ana", r.rankingClientes().getFirst().nome());
        assertEquals(2, r.rankingClientes().getFirst().pedidos());
        assertEquals("Barato", r.produtosPorQuantidade().getFirst().nome());
        assertEquals("Caro", r.produtosPorValor().getFirst().nome());
    }

    @Test void deveRecusarFiltrosInvalidosAntesDeConsultarBanco() {
        assertThrows(IllegalArgumentException.class, () -> service.resumir(inicio, null, 30));
        assertThrows(IllegalArgumentException.class, () -> service.resumir(fim, inicio, 30));
        assertThrows(IllegalArgumentException.class, () -> service.resumir(inicio, LocalDate.of(2026, 10, 22), 30));
        assertThrows(IllegalArgumentException.class, () -> service.resumir(null, null, 0));
        assertThrows(IllegalArgumentException.class, () -> service.resumir(null, null, 366));
        verify(pedidos, never()).findAll(); verify(pedidos, never()).findByCriadoEmGreaterThanEqualAndCriadoEmLessThan(any(), any());
    }

    private Produto produto(int id, String nome, String preco, int estoque) {
        Produto p = new Produto(nome, new BigDecimal(preco), estoque); p.setId(id); catalogo.add(p); return p;
    }
    private Pedido pedido(String nome, String data, StatusPedido status, Produto produto, int qtd) {
        Pedido p = new Pedido(nome); p.setId(registros.size() + 1); p.setCriadoEm(data == null ? null : LocalDateTime.parse(data));
        p.setStatus(status); p.adicionarItem(produto, qtd); registros.add(p); return p;
    }
}
