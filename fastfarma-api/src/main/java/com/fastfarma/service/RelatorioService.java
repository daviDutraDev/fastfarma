package com.fastfarma.service;

import com.fastfarma.dto.RelatorioResumo;
import com.fastfarma.dto.RelatorioResumo.*;
import com.fastfarma.model.Pedido;
import com.fastfarma.model.Produto;
import com.fastfarma.model.StatusPedido;
import com.fastfarma.repository.PedidoRepository;
import com.fastfarma.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class RelatorioService {
    private final PedidoRepository pedidos;
    private final ProdutoRepository produtos;
    private final Clock clock;

    @org.springframework.beans.factory.annotation.Autowired
    public RelatorioService(PedidoRepository pedidos, ProdutoRepository produtos) {
        this(pedidos, produtos, Clock.system(ZoneId.of("America/Sao_Paulo")));
    }

    RelatorioService(PedidoRepository pedidos, ProdutoRepository produtos, Clock clock) {
        this.pedidos = pedidos;
        this.produtos = produtos;
        this.clock = clock;
    }

    public List<Pedido> buscarPedidos(LocalDate inicio, LocalDate fim) {
        validarPeriodo(inicio, fim);
        return inicio == null ? pedidos.findAll()
                : pedidos.findByCriadoEmGreaterThanEqualAndCriadoEmLessThan(inicio.atStartOfDay(), fim.plusDays(1).atStartOfDay());
    }

    public void validarPeriodo(LocalDate inicio, LocalDate fim) {
        if ((inicio == null) != (fim == null)) {
            throw new IllegalArgumentException("Informe a data inicial e a final, ou selecione todo o período.");
        }
        if (inicio != null && (inicio.getYear() < 1900 || inicio.isAfter(fim) || fim.isAfter(LocalDate.now(clock)))) {
            throw new IllegalArgumentException("Use um período válido entre 1900 e hoje, com início anterior ou igual ao fim.");
        }
    }

    public RelatorioResumo resumir(LocalDate inicio, LocalDate fim, int coberturaDesejada) {
        if (coberturaDesejada < 1 || coberturaDesejada > 365) {
            throw new IllegalArgumentException("A cobertura desejada deve ser de 1 a 365 dias.");
        }
        List<Pedido> lista = buscarPedidos(inicio, fim);
        boolean todo = inicio == null;
        LocalDate hoje = LocalDate.now(clock);
        LocalDate efetivoInicio = todo ? lista.stream().map(Pedido::getCriadoEm).filter(Objects::nonNull)
                .map(LocalDateTime::toLocalDate).min(LocalDate::compareTo).orElse(hoje) : inicio;
        LocalDate efetivoFim = todo ? hoje : fim;
        long dias = Math.max(1, ChronoUnit.DAYS.between(efetivoInicio, efetivoFim) + 1);
        List<Pedido> validos = lista.stream().filter(p -> p.getStatus() != StatusPedido.REJEITADO).toList();
        BigDecimal valor = somar(validos);
        Map<StatusPedido, Long> status = new EnumMap<>(StatusPedido.class);
        for (StatusPedido s : StatusPedido.values()) status.put(s, lista.stream().filter(p -> p.getStatus() == s).count());

        Map<Integer, RankingProduto> ranking = new HashMap<>();
        Map<Integer, Long> demanda = new HashMap<>();
        Map<String, RankingCliente> clientes = new HashMap<>();
        for (Pedido p : validos) {
            String nome = p.getCriadoPor();
            clientes.compute(nome, (chave, anterior) -> new RankingCliente(nome,
                    anterior == null ? 1 : anterior.pedidos() + 1,
                    p.getValorTotal().add(anterior == null ? BigDecimal.ZERO : anterior.valor())));
            for (var item : p.getItens()) {
                Produto produto = item.getProduto();
                if (produto == null || produto.getId() == null) continue;
                ranking.compute(produto.getId(), (id, anterior) -> new RankingProduto(id, produto.getNome(),
                        item.getQuantidade() + (anterior == null ? 0 : anterior.unidades()),
                        item.getSubtotal().add(anterior == null ? BigDecimal.ZERO : anterior.valor())));
                // Sem data não existe base temporal confiável para estimar demanda.
                if (p.getCriadoEm() != null && !p.getCriadoEm().toLocalDate().isAfter(hoje)) {
                    demanda.merge(produto.getId(), item.getQuantidade().longValue(), Long::sum);
                }
            }
        }
        Comparator<RankingProduto> porQuantidade = Comparator.comparingLong(RankingProduto::unidades).reversed()
                .thenComparing(RankingProduto::nome, String.CASE_INSENSITIVE_ORDER).thenComparing(RankingProduto::id);
        Comparator<RankingProduto> porValor = Comparator.comparing(RankingProduto::valor).reversed()
                .thenComparing(RankingProduto::nome, String.CASE_INSENSITIVE_ORDER).thenComparing(RankingProduto::id);
        List<Reposicao> reposicao = produtos.findAll().stream()
                .map(p -> calcularReposicao(p, demanda.getOrDefault(p.getId(), 0L), dias, coberturaDesejada))
                .sorted(Comparator.comparingInt((Reposicao r) -> prioridadeOrdem(r.prioridade()))
                        .thenComparing(Reposicao::sugestao, Comparator.reverseOrder())
                        .thenComparing(Reposicao::nome, String.CASE_INSENSITIVE_ORDER)).toList();

        Comparacao comparacao = null;
        if (!todo) {
            LocalDate anteriorFim = inicio.minusDays(1);
            LocalDate anteriorInicio = inicio.minusDays(dias);
            List<Pedido> anteriores = pedidos.findByCriadoEmGreaterThanEqualAndCriadoEmLessThan(
                    anteriorInicio.atStartOfDay(), inicio.atStartOfDay()).stream()
                    .filter(p -> p.getStatus() != StatusPedido.REJEITADO).toList();
            comparacao = new Comparacao(anteriorInicio, anteriorFim, anteriores.size(), somar(anteriores));
        }
        return new RelatorioResumo(efetivoInicio, efetivoFim, todo, dias, LocalDateTime.now(clock), lista.size(),
                validos.size(), lista.stream().filter(p -> p.getCriadoEm() == null).count(), clientes.size(), valor,
                validos.isEmpty() ? null : valor.divide(BigDecimal.valueOf(validos.size()), 2, RoundingMode.HALF_UP),
                Map.copyOf(status), ranking.values().stream().sorted(porQuantidade).toList(),
                ranking.values().stream().sorted(porValor).toList(), clientes.values().stream()
                .sorted(Comparator.comparingLong(RankingCliente::pedidos).reversed()
                        .thenComparing(RankingCliente::valor, Comparator.reverseOrder())
                        .thenComparing(RankingCliente::nome)).toList(),
                evolucao(validos, efetivoInicio, efetivoFim, dias), comparacao, coberturaDesejada, reposicao);
    }

    private BigDecimal somar(List<Pedido> lista) {
        return lista.stream().map(Pedido::getValorTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Reposicao calcularReposicao(Produto p, long unidades, long dias, int alvo) {
        long estoque = p.getEstoque() == null ? 0 : p.getEstoque();
        BigDecimal media = BigDecimal.valueOf(unidades).divide(BigDecimal.valueOf(dias), 2, RoundingMode.HALF_UP);
        BigDecimal cobertura = unidades == 0 ? null : BigDecimal.valueOf(estoque).multiply(BigDecimal.valueOf(dias))
                .divide(BigDecimal.valueOf(unidades), 1, RoundingMode.HALF_UP);
        long sugestao = Math.max(0, BigDecimal.valueOf(unidades).multiply(BigDecimal.valueOf(alvo))
                .divide(BigDecimal.valueOf(dias), 0, RoundingMode.CEILING).longValueExact() - estoque);
        BigDecimal estoqueDias = BigDecimal.valueOf(estoque).multiply(BigDecimal.valueOf(dias));
        String prioridade = estoque == 0 ? "Alta" : unidades == 0 ? "Sem demanda"
                : p.estoqueBaixo() || estoqueDias.compareTo(BigDecimal.valueOf(unidades).multiply(BigDecimal.valueOf(7))) < 0 ? "Alta"
                : estoqueDias.compareTo(BigDecimal.valueOf(unidades).multiply(BigDecimal.valueOf(alvo))) < 0 ? "Média" : "Baixa";
        return new Reposicao(p.getId(), p.getNome(), estoque, unidades, media, cobertura, sugestao, prioridade);
    }

    private static int prioridadeOrdem(String prioridade) {
        return switch (prioridade) { case "Alta" -> 0; case "Média" -> 1; case "Baixa" -> 2; default -> 3; };
    }

    private List<Evolucao> evolucao(List<Pedido> lista, LocalDate inicio, LocalDate fim, long dias) {
        boolean diario = dias <= 31;
        boolean mensal = !diario && ChronoUnit.MONTHS.between(YearMonth.from(inicio), YearMonth.from(fim)) < 24;
        DateTimeFormatter formato = DateTimeFormatter.ofPattern(diario ? "dd/MM" : mensal ? "MM/yyyy" : "yyyy");
        LocalDate cursor = diario ? inicio : mensal ? inicio.withDayOfMonth(1) : inicio.withDayOfYear(1);
        List<Evolucao> pontos = new ArrayList<>();
        while (!cursor.isAfter(fim)) {
            LocalDate proximo = diario ? cursor.plusDays(1) : mensal ? cursor.plusMonths(1) : cursor.plusYears(1);
            LocalDate atual = cursor;
            List<Pedido> grupo = lista.stream().filter(p -> p.getCriadoEm() != null)
                    .filter(p -> !p.getCriadoEm().toLocalDate().isBefore(atual) && p.getCriadoEm().toLocalDate().isBefore(proximo)).toList();
            pontos.add(new Evolucao(formato.format(cursor), grupo.size(), somar(grupo)));
            cursor = proximo;
        }
        return List.copyOf(pontos);
    }
}
