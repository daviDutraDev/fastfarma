package com.fastfarma.dto;

import com.fastfarma.model.TipoUsuario;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class LoginResponse {
    private Integer id;
    private String nome;
    private String email;
    private TipoUsuario tipo;
    private String mensagem;
}
