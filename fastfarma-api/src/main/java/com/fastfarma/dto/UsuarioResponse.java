package com.fastfarma.dto;

import com.fastfarma.model.TipoUsuario;
import com.fastfarma.model.Usuario;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO de saída para {@link Usuario}.
 * Usa a fábrica {@link #de(Usuario)} para conversão.
 */
@Data
@Builder
public class UsuarioResponse {

    private Integer id;
    private String nome;
    private String email;
    private TipoUsuario tipo;
    private LocalDateTime criadoEm;

    private UsuarioResponse(Integer id, String nome, String email, TipoUsuario tipo,
                            LocalDateTime criadoEm) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.tipo = tipo;
        this.criadoEm = criadoEm;
    }

    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.getTipo(), u.getCriadoEm());
    }
}
