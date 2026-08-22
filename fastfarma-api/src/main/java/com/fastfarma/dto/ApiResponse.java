package com.fastfarma.dto;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class ApiResponse<T> {
    private boolean sucesso;
    private String mensagem;
    private T dados;

    public static <T> ApiResponse<T> ok(String mensagem, T dados) {
        return ApiResponse.<T>builder()
                .sucesso(true)
                .mensagem(mensagem)
                .dados(dados)
                .build();
    }

    public static <T> ApiResponse<T> ok(String mensagem) {
        return ApiResponse.<T>builder()
                .sucesso(true)
                .mensagem(mensagem)
                .build();
    }

    public static <T> ApiResponse<T> erro(String mensagem) {
        return ApiResponse.<T>builder()
                .sucesso(false)
                .mensagem(mensagem)
                .build();
    }
}
