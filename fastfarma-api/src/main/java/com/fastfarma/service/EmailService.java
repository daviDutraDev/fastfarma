package com.fastfarma.service;

import com.fastfarma.model.Pedido;
import com.fastfarma.model.PedidoItem;
import com.fastfarma.model.Usuario;
import jakarta.annotation.PreDestroy;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

/**
 * Servico unico de e-mail do FastFarma.
 *
 * <p>Faz tres coisas:</p>
 * <ol>
 *   <li>le a configuracao ({@code fastfarma.email.*}, com a senha vinda
 *       de variavel de ambiente);</li>
 *   <li>monta os e-mails de pedido em HTML e texto simples (criado e pronto);</li>
 *   <li>envia via SMTP (Gmail) em segundo plano, para a resposta HTTP
 *       do pedido nao esperar o servidor de e-mail.</li>
 * </ol>
 *
 * <p>Desligado por padrao ({@code fastfarma.email.enabled=false}): nesse
 * caso so registra no log.</p>
 */
@Service
@Slf4j
public class EmailService {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm", PT_BR);
    private static final int TIMEOUT_MS = 10_000;
    private static final Pattern CAMPO_TEMPLATE = Pattern.compile("\\{\\{([a-zA-Z]+)}}");

    private final boolean enabled;
    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String fromName;

    /** Um thread em segundo plano so para enviar e-mails. */
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "email-sender");
        t.setDaemon(true);
        return t;
    });

    public EmailService(
            @Value("${fastfarma.email.enabled:false}") boolean enabled,
            @Value("${fastfarma.email.host:smtp.gmail.com}") String host,
            @Value("${fastfarma.email.port:587}") int port,
            @Value("${fastfarma.email.username:}") String username,
            @Value("${fastfarma.email.password:}") String password,
            @Value("${fastfarma.email.from-name:FastFarma}") String fromName) {
        this.enabled = enabled;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.fromName = fromName;
    }

    @PreDestroy
    void encerrar() {
        executor.shutdown();
    }

    // -----------------------------------------------------------------
    // E-mails do pedido (usados pelo PedidoService)
    // -----------------------------------------------------------------

    /** Confirmacao enviada logo apos o pedido ser criado. */
    public void enviarPedidoCriado(Usuario usuario, Pedido pedido) {
        if (!temEmail(usuario, pedido)) return;

        enviarEmail(usuario.getEmail(),
                "FastFarma - Pedido #" + pedido.getId() + " recebido",
                montarCorpoCriado(usuario, pedido), montarHtmlCriado(usuario, pedido));
    }

    /** Aviso enviado quando o pedido fica PRONTO para retirada. */
    public void enviarPedidoPronto(Usuario usuario, Pedido pedido) {
        if (!temEmail(usuario, pedido)) return;

        enviarEmail(usuario.getEmail(),
                "FastFarma - Pedido #" + pedido.getId() + " pronto para retirada",
                montarCorpoPronto(usuario, pedido), montarHtmlPronto(usuario, pedido));
    }

    // -----------------------------------------------------------------
    // Montagem dos textos (package-private para teste)
    // -----------------------------------------------------------------

    String montarCorpoCriado(Usuario usuario, Pedido pedido) {
        StringBuilder sb = new StringBuilder();
        sb.append("Olá, ").append(usuario.getNome()).append("!\n\n")
                .append("Recebemos o seu pedido #").append(pedido.getId());
        if (pedido.getCriadoEm() != null) {
            sb.append(" em ").append(DATA_HORA.format(pedido.getCriadoEm()));
        }
        sb.append(".\nAssim que a farmácia separar os itens, avisaremos você por e-mail.\n\n");

        adicionarCodigo(sb, pedido);
        adicionarItens(sb, pedido);

        sb.append("\nGuarde este e-mail: o código de retirada será pedido no balcão.\n\n")
                .append("Equipe FastFarma\n");
        return sb.toString();
    }

    String montarCorpoPronto(Usuario usuario, Pedido pedido) {
        StringBuilder sb = new StringBuilder();
        sb.append("Olá, ").append(usuario.getNome()).append("!\n\n")
                .append("Boa notícia: o seu pedido #").append(pedido.getId())
                .append(" está PRONTO para retirada na farmácia.\n\n");

        adicionarCodigo(sb, pedido);
        adicionarItens(sb, pedido);

        sb.append("\nApresente o código de retirada no balcão para pegar o seu pedido.\n\n")
                .append("Equipe FastFarma\n");
        return sb.toString();
    }

    private void adicionarCodigo(StringBuilder sb, Pedido pedido) {
        sb.append("==============================\n")
                .append("CÓDIGO DE RETIRADA: ").append(pedido.getCodigoVerificacao()).append("\n")
                .append("==============================\n\n");
    }

    private void adicionarItens(StringBuilder sb, Pedido pedido) {
        sb.append("Itens do pedido:\n");
        for (PedidoItem item : pedido.getItens()) {
            String nome = item.getProduto() == null ? "?" : item.getProduto().getNome();
            sb.append(String.format(PT_BR, "  %dx %s - R$ %,.2f\n",
                    item.getQuantidade(), nome, item.getSubtotal()));
        }
        sb.append(String.format(PT_BR, "\nTotal: R$ %,.2f\n", pedido.getValorTotal()));
    }

    String montarHtmlCriado(Usuario usuario, Pedido pedido) {
        return montarHtml("pedido-criado.html", usuario, pedido);
    }

    String montarHtmlPronto(Usuario usuario, Pedido pedido) {
        return montarHtml("pedido-pronto.html", usuario, pedido);
    }

    private String montarHtml(String arquivo, Usuario usuario, Pedido pedido) {
        Map<String, String> campos = Map.of(
                "nome", escaparHtml(usuario.getNome()),
                "pedidoId", escaparHtml(pedido.getId()),
                "data", pedido.getCriadoEm() == null ? "Não informada" : DATA_HORA.format(pedido.getCriadoEm()),
                "codigo", escaparHtml(pedido.getCodigoVerificacao()),
                "itens", montarItensHtml(pedido),
                "total", String.format(PT_BR, "R$ %,.2f", pedido.getValorTotal()));

        try (var entrada = new ClassPathResource("templates/email/" + arquivo).getInputStream()) {
            String template = new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
            // Substitui apenas campos do modelo original, sem reinterpretar dados do cliente.
            return CAMPO_TEMPLATE.matcher(template).replaceAll(resultado -> {
                String valor = campos.get(resultado.group(1));
                if (valor == null) throw new IllegalStateException("Campo de e-mail desconhecido: " + resultado.group(1));
                return java.util.regex.Matcher.quoteReplacement(valor);
            });
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível carregar o modelo de e-mail: " + arquivo, e);
        }
    }

    private String montarItensHtml(Pedido pedido) {
        StringBuilder linhas = new StringBuilder();
        for (PedidoItem item : pedido.getItens()) {
            String nome = item.getProduto() == null ? "Produto indisponível" : item.getProduto().getNome();
            linhas.append("<tr><td style=\"padding:14px 8px;border-bottom:1px solid #e2e6eb;overflow-wrap:anywhere;\">")
                    .append(escaparHtml(nome))
                    .append("</td><td align=\"center\" style=\"padding:14px 4px;border-bottom:1px solid #e2e6eb;\">")
                    .append(item.getQuantidade())
                    .append("</td><td align=\"right\" style=\"padding:14px 8px;border-bottom:1px solid #e2e6eb;\">")
                    .append(String.format(PT_BR, "R$ %,.2f", item.getSubtotal()))
                    .append("</td></tr>");
        }
        return linhas.toString();
    }

    private static String escaparHtml(Object valor) {
        if (valor == null) return "";
        return valor.toString().replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    private boolean temEmail(Usuario usuario, Pedido pedido) {
        if (usuario == null || isBlank(usuario.getEmail())) {
            log.warn("[Email] Pedido #{}: cliente sem e-mail cadastrado. Notificacao ignorada.",
                    pedido.getId());
            return false;
        }
        return true;
    }

    // -----------------------------------------------------------------
    // Envio SMTP
    // -----------------------------------------------------------------

    /**
     * Envia um e-mail de texto simples em segundo plano. Nunca lanca
     * excecao: falhas so vao para o log e nao afetam o pedido.
     */
    public void enviarEmail(String destinatario, String assunto, String mensagem) {
        enviarEmail(destinatario, assunto, mensagem, null);
    }

    private void enviarEmail(String destinatario, String assunto, String mensagem, String html) {
        if (isBlank(destinatario)) {
            log.warn("[Email] Destinatario vazio - ignorando envio ({}).", assunto);
            return;
        }
        if (!enabled) {
            log.info("[Email DESATIVADO] Para {} | {}", destinatario, assunto);
            return;
        }
        if (isBlank(username) || isBlank(password)) {
            log.error("[Email] EMAIL_USERNAME/EMAIL_PASSWORD nao configurados - e-mail nao enviado.");
            return;
        }
        try {
            executor.submit(() -> enviarAgora(destinatario, assunto, mensagem, html));
        } catch (RejectedExecutionException e) {
            log.error("[Email] Envio recusado (aplicacao encerrando?): {}", e.getMessage());
        }
    }

    private void enviarAgora(String destinatario, String assunto, String mensagem, String html) {
        try {
            MimeMessage message = new MimeMessage(criarSessao());
            message.setFrom(new InternetAddress(username, fromName, "UTF-8"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            message.setSubject(assunto, "UTF-8");
            if (html == null) {
                message.setText(mensagem, "UTF-8");
            } else {
                message.setContent(montarConteudoAlternativo(mensagem, html));
            }

            Transport.send(message);
            log.info("[Email] Enviado para {} | {}", destinatario, assunto);
        } catch (Exception e) {
            log.error("[Email] Falha ao enviar para {}: {}", destinatario, e.getMessage());
        }
    }

    /** Texto primeiro e HTML depois: o leitor escolhe a versão que suporta. */
    MimeMultipart montarConteudoAlternativo(String texto, String html) throws MessagingException {
        MimeBodyPart textoParte = new MimeBodyPart();
        textoParte.setText(texto, "UTF-8");
        MimeBodyPart htmlParte = new MimeBodyPart();
        htmlParte.setText(html, "UTF-8", "html");
        MimeMultipart conteudo = new MimeMultipart("alternative");
        conteudo.addBodyPart(textoParte);
        conteudo.addBodyPart(htmlParte);
        return conteudo;
    }

    private Session criarSessao() {
        Properties smtp = new Properties();
        smtp.put("mail.smtp.auth", "true");
        smtp.put("mail.smtp.starttls.enable", "true");
        smtp.put("mail.smtp.host", host);
        smtp.put("mail.smtp.port", String.valueOf(port));
        smtp.put("mail.smtp.connectiontimeout", String.valueOf(TIMEOUT_MS));
        smtp.put("mail.smtp.timeout", String.valueOf(TIMEOUT_MS));
        smtp.put("mail.smtp.writetimeout", String.valueOf(TIMEOUT_MS));

        return Session.getInstance(smtp, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
