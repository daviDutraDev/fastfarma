package com.fastfarma.service;

import com.fastfarma.dto.RelatorioResumo;
import com.fastfarma.model.*;
import com.fastfarma.repository.PedidoRepository;
import com.fastfarma.repository.ProdutoRepository;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

class RelatorioPdfServiceTest {
    private RelatorioResumo resumo(int quantidade) {
        PedidoRepository pedidos = mock(PedidoRepository.class);
        ProdutoRepository produtos = mock(ProdutoRepository.class);
        List<Produto> catalogo = new ArrayList<>();
        List<Pedido> lista = new ArrayList<>();
        String[] nomes = {"Vitamina C 500 mg", "Protetor solar FPS 50", "Sabonete líquido", "Solução fisiológica 500 ml", "Hidratante corporal", "Curativos adesivos"};
        for (int i = 0; i < quantidade; i++) {
            Produto produto = new Produto(i < nomes.length ? nomes[i] : "Produto com nome extenso para conferir a quebra de linha no relatório número " + (i + 1),
                    new BigDecimal("12.90").add(BigDecimal.valueOf(i)), i % 5 == 0 ? 0 : i % 3 == 0 ? 3 : 20);
            produto.setId(i + 1); catalogo.add(produto);
            Pedido p = new Pedido("Cliente de exemplo " + (i % 4 + 1)); p.setId(i + 1);
            p.setCriadoEm(LocalDateTime.of(2026, 10, i % 20 + 1, 14, 0));
            p.setStatus(StatusPedido.values()[i % 4]); p.adicionarItem(produto, i % 7 + 1); lista.add(p);
        }
        when(pedidos.findAll()).thenReturn(lista); when(produtos.findAll()).thenReturn(catalogo);
        when(pedidos.findByCriadoEmGreaterThanEqualAndCriadoEmLessThan(any(), any())).thenAnswer(i -> {
            LocalDateTime de = i.getArgument(0), ate = i.getArgument(1);
            return lista.stream().filter(p -> !p.getCriadoEm().isBefore(de) && p.getCriadoEm().isBefore(ate)).toList();
        });
        return new RelatorioService(pedidos, produtos, Clock.fixed(Instant.parse("2026-10-21T15:00:00Z"), ZoneId.of("America/Sao_Paulo")))
                .resumir(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 21), 30);
    }

    @Test void deveGerarTresPdfsComTextoAcentosEPaginacao() throws Exception {
        var service = new RelatorioPdfService();
        var resumo = resumo(12);
        Map<String, byte[]> arquivos = new LinkedHashMap<>();
        arquivos.put("desempenho", service.desempenho(resumo));
        arquivos.put("geral", service.geral(resumo));
        arquivos.put("reposicao", service.reposicao(resumo));
        Path pasta = Path.of("target", "relatorio-preview"); Files.createDirectories(pasta);
        for (var arquivo : arquivos.entrySet()) {
            PdfReader reader = new PdfReader(arquivo.getValue());
            assertEquals(arquivo.getKey().equals("reposicao") ? 2 : 3, reader.getNumberOfPages(),
                    "A amostra deve manter a paginação planejada, sem páginas quase vazias.");
            StringBuilder texto = new StringBuilder();
            for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                String pagina = PdfTextExtractor.getTextFromPage(reader, i);
                assertTrue(pagina.contains("FastFarma")); assertTrue(pagina.contains("Página " + i));
                texto.append(pagina);
            }
            assertTrue(texto.toString().contains("preços atuais"));
            assertFalse(texto.toString().contains("NaN")); assertFalse(texto.toString().contains("Infinity"));
            Files.write(pasta.resolve(arquivo.getKey() + ".pdf"), arquivo.getValue()); reader.close();
        }
    }

    @Test void deveGerarPdfsVaziosComMensagemClara() throws Exception {
        var service = new RelatorioPdfService(); var vazio = resumo(0);
        for (byte[] bytes : List.of(service.desempenho(vazio), service.geral(vazio), service.reposicao(vazio))) {
            PdfReader reader = new PdfReader(bytes);
            StringBuilder texto = new StringBuilder();
            for (int i = 1; i <= reader.getNumberOfPages(); i++) texto.append(PdfTextExtractor.getTextFromPage(reader, i));
            assertTrue(texto.toString().contains("Sem registros")); assertFalse(texto.toString().contains("NaN"));
            reader.close();
        }
    }

    @Test void tabelaLongaDevePreservarPrimeiroEUltimoProduto() throws Exception {
        var service = new RelatorioPdfService(); var grande = resumo(110);
        byte[] bytes = service.reposicao(grande);
        PdfReader reader = new PdfReader(bytes);
        StringBuilder texto = new StringBuilder();
        for (int i = 1; i <= reader.getNumberOfPages(); i++) texto.append(PdfTextExtractor.getTextFromPage(reader, i));
        assertTrue(texto.toString().contains("#1)")); assertTrue(texto.toString().contains("#110)"));
        Path pasta = Path.of("target", "relatorio-preview"); Files.createDirectories(pasta);
        Files.write(pasta.resolve("reposicao-longa.pdf"), bytes); reader.close();
    }
}
