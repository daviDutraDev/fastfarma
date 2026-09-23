package com.fastfarma.controller;

/**
 * Excecao lancada quando um cliente autenticado tenta acessar recurso
 * que nao lhe pertence (ex.: ver pedido de outro cliente).
 *
 * <p>Substitui o {@code org.springframework.security.access.AccessDeniedException}
 * para nao depender de Spring Security.</p>
 */
public class AcessoNegadoException extends RuntimeException {
    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
