package com.fastfarma.dto;

import com.fastfarma.model.TipoUsuario;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class UsuarioResponse {
    private Integer id;
    private String nome;
    private String email;
    private TipoUsuario tipo;
    private LocalDateTime criadoEm;
}
