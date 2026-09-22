package com.fastfarma.notifications;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuracao do WhatsApp / Evolution API.
 *
 * <p>Mapeia o prefixo {@code fastfarma.whatsapp.evolution.*} das
 * {@code application.properties}. Todas as propriedades aceitam
 * override por variavel de ambiente:</p>
 *
 * <pre>
 *   EVOLUTION_API_URL=http://localhost:8081
 *   EVOLUTION_API_KEY=ABC-123
 *   EVOLUTION_INSTANCE=fastfarma
 *   FASTFARMA_WHATSAPP_ENABLED=true
 * </pre>
 */
@Component
@ConfigurationProperties(prefix = "fastfarma.whatsapp.evolution")
public class WhatsAppProperties {

    private String baseUrl = "http://localhost:8081";
    private String apiKey = "";
    private String instance = "fastfarma";

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getInstance() { return instance; }
    public void setInstance(String instance) { this.instance = instance; }
}
