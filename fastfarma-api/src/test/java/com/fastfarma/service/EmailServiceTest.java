package com.fastfarma.service;

import com.fastfarma.model.Pedido;
import com.fastfarma.model.Produto;
import com.fastfarma.model.Usuario;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class EmailServiceTest {
    private EmailService service;
    private Usuario usuario;
    private Pedido pedido;

    @BeforeEach
    void preparar() {
        // Envio desligado: estes testes não acessam SMTP nem usam credenciais reais.
        service = new EmailService(false, "localhost", 587, "", "", "FastFarma");
        usuario = new Usuario();
        usuario.setNome("Maria Oliveira");
        usuario.setEmail("maria@example.com");
        pedido = new Pedido(usuario.getNome());
        pedido.setId(123);
        pedido.setCodigoVerificacao(4821);
        pedido.setCriadoEm(LocalDateTime.of(2026, 10, 21, 14, 30));
        pedido.adicionarItem(new Produto("Produto de exemplo A", new BigDecimal("12.50"), 10), 2);
        pedido.adicionarItem(new Produto("Produto de exemplo B", new BigDecimal("8.90"), 10), 1);
    }

    @AfterEach
    void encerrar() {
        service.encerrar();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void modelosDevemConterDadosDoPedidoEOrientacaoCorreta(boolean pronto) throws Exception {
        String html = renderizar(pronto);
        assertAll(
                () -> assertTrue(html.contains("Maria Oliveira")),
                () -> assertTrue(html.contains("#123")),
                () -> assertTrue(html.contains("4821")),
                () -> assertTrue(html.contains("21/10/2026 às 14:30")),
                () -> assertTrue(html.contains("Produto de exemplo A")),
                () -> assertTrue(html.contains("Produto de exemplo B")),
                () -> assertTrue(html.contains("R$ 25,00")),
                () -> assertTrue(html.contains("R$ 8,90")),
                () -> assertTrue(html.contains("R$ 33,90")),
                () -> assertFalse(html.contains("{{")),
                () -> assertFalse(html.contains("localhost")),
                () -> assertTrue(html.contains(pronto ? "PRONTO PARA RETIRADA" : "PEDIDO RECEBIDO")),
                () -> assertTrue(html.contains(pronto ? "Apresente este código" : "aguarde a confirmação")));

        // Prévia com dados fictícios para inspeção visual, somente na pasta de build.
        Path pasta = Path.of("target", "email-preview");
        Files.createDirectories(pasta);
        Files.writeString(pasta.resolve(pronto ? "pedido-pronto.html" : "pedido-criado.html"), html);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void dadosDoClienteDevemSerEscapadosSemReinterpretarMarcadores(boolean pronto) {
        usuario.setNome("Ana <script> & \"Silva\" ' {{total}} $1");
        pedido.adicionarItem(new Produto("<img src=x> & {{nome}}", new BigDecimal("1.00"), 1));
        String html = renderizar(pronto);

        assertTrue(html.contains("Ana &lt;script&gt; &amp; &quot;Silva&quot; &#39; {{total}} $1"));
        assertTrue(html.contains("&lt;img src=x&gt; &amp; {{nome}}"));
        assertFalse(html.contains("<script>"));
        assertFalse(html.contains("<img src=x>"));
    }

    @Test
    void pedidoSemDataNaoDeveMostrarNull() {
        pedido.setCriadoEm(null);
        assertTrue(service.montarHtmlCriado(usuario, pedido).contains("Não informada"));
        assertTrue(service.montarHtmlPronto(usuario, pedido).contains("Não informada"));
    }

    @Test
    void mensagemDeveTransportarTextoEHtmlEmUtf8() throws Exception {
        String texto = service.montarCorpoCriado(usuario, pedido);
        String html = service.montarHtmlCriado(usuario, pedido);
        MimeMessage original = new MimeMessage(Session.getInstance(new Properties()));
        original.setContent(service.montarConteudoAlternativo(texto, html));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        original.writeTo(bytes);
        MimeMessage recebido = new MimeMessage(Session.getInstance(new Properties()),
                new ByteArrayInputStream(bytes.toByteArray()));

        assertTrue(recebido.isMimeType("multipart/alternative"));
        MimeMultipart partes = (MimeMultipart) recebido.getContent();
        assertEquals(2, partes.getCount());
        assertTrue(partes.getBodyPart(0).isMimeType("text/plain"));
        assertTrue(partes.getBodyPart(1).isMimeType("text/html"));
        assertTrue(partes.getBodyPart(1).getContentType().toUpperCase().contains("UTF-8"));
        // O transporte MIME normaliza quebras de linha para CRLF.
        assertEquals(texto.replace("\r\n", "\n"), partes.getBodyPart(0).getContent().toString().replace("\r\n", "\n"));
        assertEquals(html.replace("\r\n", "\n"), partes.getBodyPart(1).getContent().toString().replace("\r\n", "\n"));
    }

    @Test
    void versaoTextoDevePreservarResumoECodigoNosDoisAvisos() {
        for (String texto : new String[]{service.montarCorpoCriado(usuario, pedido),
                service.montarCorpoPronto(usuario, pedido)}) {
            assertTrue(texto.contains("CÓDIGO DE RETIRADA: 4821"));
            assertTrue(texto.contains("2x Produto de exemplo A - R$ 25,00"));
            assertTrue(texto.contains("Total: R$ 33,90"));
            assertFalse(texto.contains("<table"));
        }
    }

    private String renderizar(boolean pronto) {
        return pronto ? service.montarHtmlPronto(usuario, pedido) : service.montarHtmlCriado(usuario, pedido);
    }
}
