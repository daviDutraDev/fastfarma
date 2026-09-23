package com.fastfarma.controller;

import com.fastfarma.model.Pedido;
import com.fastfarma.model.Produto;
import com.fastfarma.model.StatusPedido;
import com.fastfarma.repository.PedidoRepository;
import com.fastfarma.repository.ProdutoRepository;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

/**
 * Endpoints de relatorio em PDF usando iText.
 *
 * <p>Apenas FUNCIONARIO tem acesso (definido em SecurityBlockerFilter).
 * Os PDFs sao gerados em memoria e retornados como application/pdf
 * com filename apropriado para download.</p>
 */
@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;

    public RelatorioController(PedidoRepository pedidoRepository,
                               ProdutoRepository produtoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
    }

    /** Relatorio geral de pedidos em PDF. */
    @GetMapping("/pedidos")
    public ResponseEntity<byte[]> relatorioPedidos() {
        List<Pedido> pedidos = pedidoRepository.findAll();
        pedidos.sort(Comparator.comparing(Pedido::getId).reversed());

        byte[] pdf = gerarPdf("Relatorio de Pedidos", "Listagem completa de pedidos",
                new String[]{"ID", "Cliente", "Status", "Itens", "Valor (R$)", "Criado em"},
                pedidos.stream().map(this::pedidoParaLinha).toArray(Object[][]::new));

        return pdfResponse(pdf, "relatorio-pedidos.pdf");
    }

    /** Relatorio filtrado por status. */
    @GetMapping("/pedidos/status")
    public ResponseEntity<byte[]> relatorioPedidosPorStatus(String status) {
        StatusPedido s;
        try {
            s = StatusPedido.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            s = null;
        }
        List<Pedido> pedidos = s == null
                ? pedidoRepository.findAll()
                : pedidoRepository.findByStatus(s);
        pedidos.sort(Comparator.comparing(Pedido::getId).reversed());

        String titulo = s == null ? "Relatorio de Pedidos" : "Relatorio de Pedidos - " + s;
        byte[] pdf = gerarPdf(titulo, "Filtrado por status: " + (s == null ? "TODOS" : s),
                new String[]{"ID", "Cliente", "Status", "Itens", "Valor (R$)", "Criado em"},
                pedidos.stream().map(this::pedidoParaLinha).toArray(Object[][]::new));

        return pdfResponse(pdf, "relatorio-pedidos-" + (s == null ? "todos" : s.name().toLowerCase()) + ".pdf");
    }

    /** Catalogo de produtos em PDF. */
    @GetMapping("/produtos")
    public ResponseEntity<byte[]> relatorioProdutos() {
        List<Produto> produtos = produtoRepository.findAll();
        produtos.sort(Comparator.comparing(Produto::getNome, String.CASE_INSENSITIVE_ORDER));

        byte[] pdf = gerarPdf("Catalogo de Produtos", "Listagem do catalogo",
                new String[]{"ID", "Nome", "Categoria", "Preco (R$)", "Estoque", "Situacao"},
                produtos.stream().map(this::produtoParaLinha).toArray(Object[][]::new));

        return pdfResponse(pdf, "relatorio-produtos.pdf");
    }

    // -----------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------

    private Object[] pedidoParaLinha(Pedido p) {
        int qtdItens = p.getItens() == null ? 0 : p.getItens().size();
        BigDecimal valor = p.getValorTotal();
        String criadoEm = p.getCriadoEm() == null ? "-" : DATA_HORA.format(p.getCriadoEm());
        return new Object[]{
                "#" + p.getId(),
                p.getCriadoPor(),
                p.getStatus() == null ? "-" : p.getStatus().name(),
                qtdItens + " item(s)",
                valor == null ? "0,00" : String.format("%.2f", valor),
                criadoEm
        };
    }

    private Object[] produtoParaLinha(Produto p) {
        return new Object[]{
                "#" + p.getId(),
                p.getNome(),
                p.getCategoria() == null || p.getCategoria().isBlank() ? "-" : p.getCategoria(),
                String.format("%.2f", p.getPreco()),
                p.getEstoque(),
                p.getSituacao()
        };
    }

    private byte[] gerarPdf(String titulo, String subtitulo, String[] cabecalho, Object[][] linhas) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            Font fTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font fSubtitulo = FontFactory.getFont(FontFactory.HELVETICA, 11, Font.ITALIC);
            Font fCabecalho = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font fCelula = FontFactory.getFont(FontFactory.HELVETICA, 9);

            Paragraph pTitulo = new Paragraph(titulo, fTitulo);
            pTitulo.setAlignment(Element.ALIGN_LEFT);
            doc.add(pTitulo);

            Paragraph pSubtitulo = new Paragraph(subtitulo, fSubtitulo);
            pSubtitulo.setAlignment(Element.ALIGN_LEFT);
            pSubtitulo.setSpacingAfter(12f);
            doc.add(pSubtitulo);

            PdfPTable table = new PdfPTable(cabecalho.length);
            table.setWidthPercentage(100);

            for (String h : cabecalho) {
                PdfPCell cell = new PdfPCell(new Paragraph(h, fCabecalho));
                // Destaque visual sem depender de Color (alguns ambientes
                // nao enxergam com.itextpdf.text.Color).
                cell.setBorderWidthTop(1.2f);
                cell.setBorderWidthBottom(0.8f);
                cell.setPadding(5);
                table.addCell(cell);
            }

            for (Object[] linha : linhas) {
                for (Object campo : linha) {
                    PdfPCell cell = new PdfPCell(new Paragraph(String.valueOf(campo), fCelula));
                    cell.setPadding(4);
                    table.addCell(cell);
                }
            }

            doc.add(table);

            Paragraph pRodape = new Paragraph(
                    "\nGerado em " + DATA_HORA.format(java.time.LocalDateTime.now())
                            + " - FastFarma",
                    FontFactory.getFont(FontFactory.HELVETICA, 8, Font.ITALIC));
            pRodape.setAlignment(Element.ALIGN_RIGHT);
            doc.add(pRodape);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Falha ao gerar PDF: " + e.getMessage(), e);
        }
    }

    private ResponseEntity<byte[]> pdfResponse(byte[] pdf, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
