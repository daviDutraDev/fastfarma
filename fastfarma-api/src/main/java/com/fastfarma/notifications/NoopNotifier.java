package com.fastfarma.notifications;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

/**
 * Stub de NotificationService usado quando nenhum outro provider esta
 * registrado. Apenas loga a mensagem para garantir que o sistema
 * continua funcionando em dev/testes quando o WhatsApp esta desligado.
 */
@Service
@Slf4j
@ConditionalOnMissingBean(NotificationService.class)
public class NoopNotifier implements NotificationService {

    @Override
    public boolean enviarWhatsApp(String telefone, String mensagem) {
        log.info("[NoopNotifier] Para {}: {}", telefone, mensagem);
        return true;
    }
}
