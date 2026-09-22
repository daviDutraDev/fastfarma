package com.fastfarma.notifications;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

/**
 * Implementacao de {@link NotificationService} usando a Evolution API.
 *
 * <p>Endpoint esperado (Evolution v2):</p>
 * <pre>
 *   POST {baseUrl}/message/sendText/{instance}
 *   Headers: apikey: &lt;apiKey&gt;, Content-Type: application/json
 *   Body: { "number": "5511999999999", "text": "mensagem" }
 * </pre>
 *
 * <p>Se {@code fastfarma.whatsapp.enabled=false} (padrao em dev), a
 * implementacao nao faz HTTP — apenas loga a mensagem. Ative quando
 * tiver o container da Evolution rodando.</p>
 *
 * <p>Referencia: <a href="https://doc.evolution-api.com/v2/api-reference/message/send-text">
 * Evolution API docs</a>.</p>
 */
@Service
@Slf4j
@ConditionalOnProperty(prefix = "fastfarma.whatsapp", name = "provider",
        havingValue = "evolution", matchIfMissing = true)
public class EvolutionApiNotifier implements NotificationService {

    private final WebClient client;
    private final WhatsAppProperties props;
    private final boolean enabled;

    public EvolutionApiNotifier(
            WhatsAppProperties props,
            @Value("${fastfarma.whatsapp.enabled:false}") boolean enabled) {
        this.props = props;
        this.enabled = enabled;
        WebClient.Builder builder = WebClient.builder()
                .baseUrl(props.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .codecs(c -> c.defaultCodecs().maxInMemorySize(256 * 1024));
        if (props.getApiKey() != null && !props.getApiKey().isBlank()) {
            builder.defaultHeader("apikey", props.getApiKey());
        }
        this.client = builder.build();
    }

    @Override
    public boolean enviarWhatsApp(String telefone, String mensagem) {
        if (telefone == null || telefone.isBlank()) {
            log.warn("Telefone vazio — ignorando envio de WhatsApp.");
            return false;
        }
        // Normaliza para 55 + DDD + numero (formato esperado pela Evolution).
        String numero = normalizarNumero(telefone);

        if (!enabled) {
            log.info("[WhatsApp DESATIVADO] Para {}: {}", numero, mensagem);
            return true;
        }

        try {
            var response = client.post()
                    .uri("/message/sendText/{instance}", props.getInstance())
                    .bodyValue(Map.of("number", numero, "text", mensagem))
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(Duration.ofSeconds(8))
                    .block();

            boolean ok = response != null && response.getStatusCode().is2xxSuccessful();
            if (ok) {
                log.info("WhatsApp enviado para {}", numero);
            } else {
                log.warn("Falha no envio do WhatsApp para {} (status={})",
                        numero, response == null ? "null" : response.getStatusCode());
            }
            return ok;
        } catch (Exception ex) {
            log.error("Erro ao enviar WhatsApp para {}: {}", numero, ex.getMessage());
            return false;
        }
    }

    private String normalizarNumero(String telefone) {
        String digits = telefone.replaceAll("\\D", "");
        // Se nao comeca com 55, prefixa (BR como default).
        if (!digits.startsWith("55") && digits.length() <= 11) {
            digits = "55" + digits;
        }
        return digits;
    }
}
