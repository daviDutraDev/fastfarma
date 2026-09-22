package com.fastfarma.dto;

import com.fastfarma.model.TipoUsuario;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private Integer id;
    private String nome;
    private String email;
    private TipoUsuario tipo;
    private String telefone;
    /** Token JWT a ser enviado em Authorization: Bearer <token>. */
    private String token;
    /** Tipo do token (sempre "Bearer"). */
    private String tokenType;
    /** Segundos até a expiração (para o frontend saber quando renovar). */
    private Long expiresInSeconds;
    private String mensagem;
}
