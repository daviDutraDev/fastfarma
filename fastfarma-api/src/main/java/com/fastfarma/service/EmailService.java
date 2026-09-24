package com.fastfarma.service;

import com.fastfarma.model.Pedido;
import com.fastfarma.model.PedidoItem;
import com.fastfarma.model.Usuario;
import jakarta.annotation.PreDestroy;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Properties;
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
 *   <li>monta o texto dos e-mails de pedido (criado e pronto);</li>
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
                montarCorpoCriado(usuario, pedido));
    }

    /** Aviso enviado quando o pedido fica PRONTO para retirada. */
    public void enviarPedidoPronto(Usuario usuario, Pedido pedido) {
        if (!temEmail(usuario, pedido)) return;

        enviarEmail(usuario.getEmail(),
                "FastFarma - Pedido #" + pedido.getId() + " pronto para retirada",
                montarCorpoPronto(usuario, pedido));
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
            sb.append(String.format(PT_BR, "  %dx %s - R$ %,.2f\n"
                  ,nome, item.getSubtotal()));
        }
        sb.append(String.format(PT_BR, "\nTotal: R$ %,.2f\n", pedido.getValorTotal()));
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
            executor.submit(() -> enviarAgora(destinatario, assunto, mensagem));
        } catch (RejectedExecutionException e) {
            log.error("[Email] Envio recusado (aplicacao encerrando?): {}", e.getMessage());
        }
    }

    private void enviarAgora(String destinatario, String assunto, String mensagem) {
        try {
            MimeMessage message = new MimeMessage(criarSessao());
            message.setFrom(new InternetAddress(username, fromName, "UTF-8"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            message.setSubject(assunto, "UTF-8");
            message.setText(mensagem, "UTF-8");

            Transport.send(message);
            log.info("[Email] Enviado para {} | {}", destinatario, assunto);
        } catch (Exception e) {
            log.error("[Email] Falha ao enviar para {}: {}", destinatario, e.getMessage());
        }
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