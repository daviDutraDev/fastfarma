package com.fastfarma.notifications;

/**
 * Interface de notificações. Hoje só temos WhatsApp via Evolution API,
 * mas a interface permite trocar o provider sem mexer nos call sites.
 */
public interface NotificationService {

    /**
     * Envia uma mensagem de texto para um numero (com DDD, so digitos).
     *
     * @param telefone destino (somente digitos, 10-11 chars)
     * @param mensagem texto a enviar (limite WhatsApp: ~4096 chars)
     * @return true se enviou, false caso contrario (sem lancar excecao)
     */
    boolean enviarWhatsApp(String telefone, String mensagem);
}
