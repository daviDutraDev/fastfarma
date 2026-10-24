package com.fastfarma.service;

import com.fastfarma.dto.RelatorioResumo;
import com.fastfarma.dto.RelatorioResumo.*;
import com.fastfarma.model.StatusPedido;
import com.fastfarma.model.Produto;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/** PDFs com gráficos vetoriais, sem dependências ou serviços externos. */
@Service
public class RelatorioPdfService {
    private static final BaseColor AZUL = new BaseColor(82, 111, 149);
    private static final BaseColor ESCURO = new BaseColor(40, 53, 72);
    private static final BaseColor CINZA = new BaseColor(89, 101, 121);
    private static final BaseColor CLARO = new BaseColor(241, 244, 248);
    private static final BaseColor VERDE = new BaseColor(29, 122, 93);
    private static final Locale BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] desempenho(RelatorioResumo r) {
        return gerar("Desempenho de pedidos", r, (doc, writer) -> {
            titulo(doc, "Resultados do período", "Pedidos rejeitados ficam fora dos valores, médias e rankings.");
            indicadores(doc, new String[]{"Pedidos válidos", "Valor dos pedidos", "Valor médio / pedido"},
                    new String[]{"" + r.validos(), moeda(r.valor()), r.media() == null ? "Sem pedidos" : moeda(r.media())});
            secao(doc, "Evolução dos pedidos");
            linha(doc, writer, r.evolucao(), false);
            secao(doc, "Evolução do valor dos pedidos (R$)");
            linha(doc, writer, r.evolucao(), true);
            nota(doc, "As datas sem pedidos também entram no intervalo. Pedidos sem data não aparecem nos gráficos.");

            doc.newPage();
            titulo(doc, "Produtos mais pedidos", "Ranking por quantidade de unidades, com valor acumulado por produto.");
            barras(doc, writer, r.produtosPorQuantidade().stream().limit(8).map(RankingProduto::nome).toList(),
                    r.produtosPorQuantidade().stream().limit(8).map(p -> (double) p.unidades()).toList(), "un.");
            tabelaProdutos(doc, r.produtosPorQuantidade());

            doc.newPage();
            titulo(doc, "Produtos por valor e clientes", "Rankings do período, excluindo pedidos rejeitados.");
            secao(doc, "Produtos com maior valor acumulado");
            tabelaProdutos(doc, r.produtosPorValor());
            secao(doc, "Clientes com mais pedidos");
            tabelaClientes(doc, r.rankingClientes());
        });
    }

    public byte[] geral(RelatorioResumo r) {
        return gerar("Relatório geral da farmácia", r, (doc, writer) -> {
            titulo(doc, "Visão geral", "Pedidos válidos: todos, exceto rejeitados. "
                    + r.status().getOrDefault(StatusPedido.PENDENTE, 0L) + " pedidos do período ainda pendentes.");
            indicadores(doc, new String[]{"Pedidos recebidos", "Pedidos válidos", "Clientes com pedidos"},
                    new String[]{"" + r.recebidos(), "" + r.validos(), "" + r.clientes()});
            indicadores(doc, new String[]{"Valor dos pedidos", "Valor médio / pedido", "Taxa de rejeição"},
                    new String[]{moeda(r.valor()), r.media() == null ? "Sem pedidos" : moeda(r.media()),
                            r.recebidos() == 0 ? "Sem pedidos" : numero(100d * r.status().getOrDefault(StatusPedido.REJEITADO, 0L) / r.recebidos()) + "%"});
            secao(doc, "Comparação com o período anterior");
            if (r.comparacao() == null) {
                nota(doc, "Não se aplica a todo o período. Escolha um intervalo para comparar períodos de igual duração.");
            } else {
                var c = r.comparacao();
                nota(doc, "Período anterior: " + DATA.format(c.inicio()) + " a " + DATA.format(c.fim()) + ". Mesma duração; pedidos rejeitados excluídos.");
                tabela(doc, new String[]{"Indicador", "Anterior", "Atual", "Variação"}, new float[]{2.1f, 1.4f, 1.4f, 1.9f}, List.of(
                        new String[]{"Pedidos válidos", "" + c.pedidos(), "" + r.validos(), variacao(BigDecimal.valueOf(r.validos()), BigDecimal.valueOf(c.pedidos()))},
                        new String[]{"Valor dos pedidos", moeda(c.valor()), moeda(r.valor()), variacao(r.valor(), c.valor())}));
            }
            secao(doc, "Distribuição dos pedidos por status");
            barras(doc, writer, List.of("Pendente", "Aprovado", "Pronto", "Rejeitado"),
                    List.of(contagem(r, StatusPedido.PENDENTE), contagem(r, StatusPedido.APROVADO), contagem(r, StatusPedido.PRONTO), contagem(r, StatusPedido.REJEITADO)), "ped.");

            doc.newPage();
            titulo(doc, "Destaques do período", "Resumo dos cinco primeiros colocados em cada ranking.");
            secao(doc, "5 produtos mais pedidos");
            tabelaProdutos(doc, r.produtosPorQuantidade().stream().limit(5).toList());
            secao(doc, "5 produtos com maior valor acumulado");
            tabelaProdutos(doc, r.produtosPorValor().stream().limit(5).toList());
            secao(doc, "5 clientes com mais pedidos");
            tabelaClientes(doc, r.rankingClientes().stream().limit(5).toList());

            doc.newPage();
            titulo(doc, "Estoque e reposição", "Posição atual, consultada na data de geração. Não representa o estoque histórico do período.");
            indicadores(doc, new String[]{"Produtos cadastrados", "Estoque baixo*", "Esgotados"}, new String[]{"" + r.estoque().size(),
                    "" + r.estoque().stream().filter(p -> p.estoque() > 0 && p.estoque() <= Produto.ESTOQUE_MINIMO_ALERTA).count(),
                    "" + r.estoque().stream().filter(p -> p.estoque() == 0).count()});
            nota(doc, "*Estoque baixo: de 1 a " + Produto.ESTOQUE_MINIMO_ALERTA + " unidades. Esgotados são contados separadamente.");
            secao(doc, "Produtos que precisam de atenção");
            tabela(doc, new String[]{"Produto", "Estoque atual", "Situação"}, new float[]{3, 1, 1.5f},
                    r.estoque().stream().filter(p -> p.estoque() <= Produto.ESTOQUE_MINIMO_ALERTA)
                            .map(p -> new String[]{p.nome() + " (#" + p.id() + ")", "" + p.estoque(), p.estoque() == 0 ? "Esgotado" : "Estoque baixo"}).toList());
            secao(doc, "Evolução dos pedidos válidos");
            linha(doc, writer, r.evolucao(), false);
        });
    }

    public byte[] reposicao(RelatorioResumo r) {
        return gerar("Reposição de estoque", r, (doc, writer) -> {
            titulo(doc, "Planejamento de reposição", "Estimativa baseada em unidades pedidas, excluindo pedidos rejeitados.");
            indicadores(doc, new String[]{"Dias analisados", "Cobertura desejada", "Unidades sugeridas"},
                    new String[]{"" + r.dias(), r.coberturaDesejada() + " dias", "" + r.estoque().stream().mapToLong(Reposicao::sugestao).sum()});
            secao(doc, "Como interpretar");
            nota(doc, "Média diária = unidades pedidas / " + r.dias() + " dias corridos. Cobertura = estoque atual / média diária. "
                    + "Reposição sugerida = arredondamento para cima da demanda para " + r.coberturaDesejada() + " dias, menos o estoque atual (mínimo zero).");
            nota(doc, "Sem demanda no período: cobertura sem estimativa e sugestão zero. Revise manualmente esses produtos, especialmente os esgotados. "
                    + "Produtos com pouca procura podem ter média exibida como 0,00; os cálculos usam a razão sem arredondar.");
            nota(doc, "Prioridades calculadas antes do arredondamento. Alta: esgotado, estoque baixo ou cobertura inferior a 7 dias. Média: cobertura menor que a desejada. "
                    + "Baixa: cobertura suficiente. Sem demanda: produto com estoque e sem pedidos no período.");
            secao(doc, "Maiores necessidades de reposição (unidades)");
            List<Reposicao> maiores = r.estoque().stream().filter(p -> p.sugestao() > 0)
                    .sorted(java.util.Comparator.comparingLong(Reposicao::sugestao).reversed()).limit(8).toList();
            barras(doc, writer, maiores.stream().map(Reposicao::nome).toList(), maiores.stream().map(p -> (double) p.sugestao()).toList(), "un.");
            nota(doc, "Projeção de apoio à compra; não inclui prazo de entrega do fornecedor, sazonalidade ou vendas fora do sistema.");

            doc.newPage();
            titulo(doc, "Plano por produto", "Estoque na data de geração. Ordenado por prioridade e quantidade sugerida.");
            tabela(doc, new String[]{"Produto", "Estoque", "Unid. pedidas", "Média / dia", "Cobertura (dias)", "Repor (un.)", "Prioridade"},
                    new float[]{2.65f, 1, 1, 1, 1.1f, 1, 1.2f}, r.estoque().stream().map(p -> new String[]{
                            p.nome() + " (#" + p.id() + ")", "" + p.estoque(), "" + p.unidades(), decimal(p.mediaDiaria()),
                            p.cobertura() == null ? "Sem base" : decimal(p.cobertura()), "" + p.sugestao(), p.prioridade()}).toList());
        });
    }

    private byte[] gerar(String nome, RelatorioResumo r, Conteudo conteudo) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4, 36, 36, 68, 48);
            PdfWriter writer = PdfWriter.getInstance(doc, bytes);
            writer.setPageEvent(new PdfPageEventHelper() {
                @Override public void onEndPage(PdfWriter w, Document d) {
                    PdfContentByte cb = w.getDirectContent();
                    cb.setColorStroke(AZUL); cb.setLineWidth(2);
                    cb.moveTo(36, 794); cb.lineTo(559, 794); cb.stroke();
                    ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, new Phrase("FastFarma", fonte(15, true, ESCURO)), 36, 809, 0);
                    ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, new Phrase(nome, fonte(9, false, CINZA)), 559, 809, 0);
                    ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, new Phrase("Gerado em " + r.geradoEm().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + " (São Paulo)", fonte(8, false, CINZA)), 36, 28, 0);
                    ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, new Phrase("Página " + w.getPageNumber(), fonte(8, false, CINZA)), 559, 28, 0);
                }
            });
            doc.addTitle(nome); doc.addAuthor("FastFarma"); doc.open();
            nota(doc, (r.todoPeriodo() ? "Todo o período | Base temporal: " : "Período: ") + DATA.format(r.inicio()) + " a " + DATA.format(r.fim()));
            nota(doc, "Valores calculados com os preços atuais do catálogo; não representam pagamentos confirmados. Clientes agrupados pelo nome registrado no pedido.");
            if (r.semData() > 0) nota(doc, r.semData() + " pedidos sem data incluídos no resumo; excluídos da evolução e da estimativa de demanda.");
            conteudo.adicionar(doc, writer);
            doc.close();
            return bytes.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Não foi possível gerar o relatório em PDF.", e);
        }
    }

    private void titulo(Document doc, String titulo, String subtitulo) throws DocumentException {
        Paragraph p = new Paragraph(titulo, fonte(23, true, ESCURO)); p.setSpacingBefore(8); p.setSpacingAfter(9); p.setKeepTogether(true); doc.add(p);
        nota(doc, subtitulo);
    }

    private void secao(Document doc, String texto) throws DocumentException {
        Paragraph p = new Paragraph(texto, fonte(13, true, ESCURO)); p.setSpacingBefore(14); p.setSpacingAfter(10); p.setKeepTogether(true); doc.add(p);
    }

    private void nota(Document doc, String texto) throws DocumentException {
        Paragraph p = new Paragraph(texto, fonte(9, false, CINZA)); p.setLeading(13); p.setSpacingAfter(8); doc.add(p);
    }

    private void indicadores(Document doc, String[] nomes, String[] valores) throws DocumentException {
        PdfPTable table = new PdfPTable(nomes.length); table.setWidthPercentage(100); table.setSpacingBefore(8); table.setSpacingAfter(8);
        for (int i = 0; i < nomes.length; i++) {
            PdfPCell cell = new PdfPCell(); cell.setBackgroundColor(CLARO); cell.setBorderColor(BaseColor.WHITE); cell.setBorderWidth(3); cell.setPadding(12);
            cell.addElement(new Paragraph(nomes[i], fonte(9, false, CINZA)));
            cell.addElement(new Paragraph(valores[i], fonte(17, true, AZUL))); table.addCell(cell);
        }
        doc.add(table);
    }

    private void tabelaProdutos(Document doc, List<RankingProduto> lista) throws DocumentException {
        tabela(doc, new String[]{"Produto", "Unidades", "Valor acumulado"}, new float[]{3.4f, 1, 1.6f},
                lista.stream().map(p -> new String[]{p.nome() + " (#" + p.id() + ")", "" + p.unidades(), moeda(p.valor())}).toList());
    }

    private void tabelaClientes(Document doc, List<RankingCliente> lista) throws DocumentException {
        tabela(doc, new String[]{"Cliente (nome registrado)", "Pedidos", "Valor acumulado"}, new float[]{3.4f, 1, 1.6f},
                lista.stream().map(p -> new String[]{p.nome(), "" + p.pedidos(), moeda(p.valor())}).toList());
    }

    private void tabela(Document doc, String[] colunas, float[] larguras, List<String[]> linhas) throws DocumentException {
        PdfPTable table = new PdfPTable(colunas.length); table.setWidthPercentage(100); table.setWidths(larguras);
        table.setHeaderRows(1); table.setSplitLate(false); table.setSpacingAfter(10);
        for (String coluna : colunas) {
            PdfPCell cell = new PdfPCell(new Phrase(coluna, fonte(9, true, BaseColor.WHITE)));
            cell.setBackgroundColor(AZUL); cell.setBorderColor(BaseColor.WHITE); cell.setPadding(8); table.addCell(cell);
        }
        if (linhas.isEmpty()) {
            PdfPCell cell = new PdfPCell(new Phrase("Sem registros para os critérios selecionados.", fonte(10, false, CINZA)));
            cell.setColspan(colunas.length); cell.setPadding(14); cell.setBorderColor(CLARO); table.addCell(cell);
        }
        for (int i = 0; i < linhas.size(); i++) {
            for (String texto : linhas.get(i)) {
                PdfPCell cell = new PdfPCell(new Phrase(texto, fonte(9, false, ESCURO))); cell.setPadding(7);
                cell.setBorderColor(BaseColor.WHITE); cell.setBackgroundColor(i % 2 == 0 ? CLARO : BaseColor.WHITE); table.addCell(cell);
            }
        }
        doc.add(table);
    }

    private void barras(Document doc, PdfWriter writer, List<String> nomes, List<Double> valores, String unidade) throws Exception {
        if (nomes.isEmpty() || valores.stream().allMatch(v -> v == 0)) { nota(doc, "Sem dados para exibir neste gráfico."); return; }
        float altura = nomes.size() * 28 + 12;
        PdfTemplate desenho = writer.getDirectContent().createTemplate(523, altura);
        double maximo = valores.stream().mapToDouble(Double::doubleValue).max().orElse(1);
        for (int i = 0; i < nomes.size(); i++) {
            float y = altura - (i + 1) * 28;
            String rotulo = abreviar(nomes.get(i), 25);
            ColumnText.showTextAligned(desenho, Element.ALIGN_LEFT, new Phrase(rotulo, fonte(9, false, ESCURO)), 0, y + 5, 0);
            desenho.setColorFill(CLARO); desenho.rectangle(160, y, 285, 19); desenho.fill();
            desenho.setColorFill(AZUL); desenho.rectangle(160, y, (float) (285 * valores.get(i) / maximo), 19); desenho.fill();
            ColumnText.showTextAligned(desenho, Element.ALIGN_RIGHT, new Phrase(String.format(BR, "%,.0f %s", valores.get(i), unidade), fonte(9, true, ESCURO)), 523, y + 5, 0);
        }
        doc.add(Image.getInstance(desenho));
    }

    private void linha(Document doc, PdfWriter writer, List<Evolucao> pontos, boolean valor) throws Exception {
        if (pontos.isEmpty() || pontos.stream().allMatch(p -> valor ? p.valor().signum() == 0 : p.pedidos() == 0)) {
            nota(doc, "Sem pedidos válidos com data neste período."); return;
        }
        PdfTemplate desenho = writer.getDirectContent().createTemplate(523, 145);
        double maximo = pontos.stream().mapToDouble(p -> valor ? p.valor().doubleValue() : p.pedidos()).max().orElse(1);
        int divisoes = valor ? 3 : (int) Math.min(3, Math.ceil(maximo));
        // Contagens têm marcações inteiras, mantendo o eixo e os pontos na mesma escala.
        double escala = valor ? maximo : Math.ceil(maximo / divisoes) * divisoes;
        desenho.setColorStroke(new BaseColor(220, 226, 234)); desenho.setLineWidth(.6f);
        for (int i = 0; i <= divisoes; i++) {
            float y = 30 + 95f * i / divisoes;
            desenho.moveTo(55, y); desenho.lineTo(495, y); desenho.stroke();
            String eixo = valor ? numero(escala * i / divisoes) : String.format(BR, "%,.0f", escala * i / divisoes);
            ColumnText.showTextAligned(desenho, Element.ALIGN_RIGHT, new Phrase(eixo, fonte(8, false, CINZA)), 49, y - 3, 0);
        }
        desenho.setColorStroke(valor ? VERDE : AZUL); desenho.setLineWidth(2);
        for (int i = 0; i < pontos.size(); i++) {
            float x = pontos.size() == 1 ? 275 : 55 + 440f * i / (pontos.size() - 1);
            double n = valor ? pontos.get(i).valor().doubleValue() : pontos.get(i).pedidos();
            float y = 30 + (float) (95 * n / escala);
            if (i == 0) desenho.moveTo(x, y); else desenho.lineTo(x, y);
        }
        desenho.stroke();
        int passo = Math.max(1, (int) Math.ceil(pontos.size() / 6d));
        for (int i = 0; i < pontos.size(); i++) {
            float x = pontos.size() == 1 ? 275 : 55 + 440f * i / (pontos.size() - 1);
            double n = valor ? pontos.get(i).valor().doubleValue() : pontos.get(i).pedidos();
            desenho.setColorFill(valor ? VERDE : AZUL); desenho.circle(x, 30 + (float) (95 * n / escala), 2.4f); desenho.fill();
            if (i % passo == 0 || (i == pontos.size() - 1 && (i % passo) > passo / 2)) {
                ColumnText.showTextAligned(desenho, Element.ALIGN_CENTER, new Phrase(pontos.get(i).periodo(), fonte(8, false, CINZA)), x, 12, 0);
            }
        }
        doc.add(Image.getInstance(desenho));
    }

    private double contagem(RelatorioResumo r, StatusPedido status) { return r.status().getOrDefault(status, 0L).doubleValue(); }
    private static String abreviar(String texto, int tamanho) { return texto.length() <= tamanho ? texto : texto.substring(0, tamanho - 3) + "..."; }
    private static Font fonte(float tamanho, boolean negrito, BaseColor cor) { return FontFactory.getFont(FontFactory.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED, tamanho, negrito ? Font.BOLD : Font.NORMAL, cor); }
    private static String moeda(BigDecimal valor) { return String.format(BR, "R$ %,.2f", valor); }
    private static String numero(double valor) { return String.format(BR, "%,.1f", valor); }
    private static String decimal(BigDecimal valor) { return String.format(BR, "%,.2f", valor); }
    private static String variacao(BigDecimal atual, BigDecimal anterior) {
        if (anterior.signum() == 0) return atual.signum() == 0 ? "0,0%" : "Sem base anterior";
        BigDecimal percentual = atual.subtract(anterior).multiply(BigDecimal.valueOf(100)).divide(anterior, 1, RoundingMode.HALF_UP);
        return (percentual.signum() > 0 ? "+" : "") + String.format(BR, "%,.1f%%", percentual);
    }

    @FunctionalInterface private interface Conteudo { void adicionar(Document doc, PdfWriter writer) throws Exception; }
}
