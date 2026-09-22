package com.fastfarma.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CadastroRequest {
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String nome;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 4, message = "Senha deve ter pelo menos 4 caracteres")
    private String senha;

    /** Telefone opcional com DDD. Aceita caracteres não numéricos que serão removidos. */
    @Pattern(regexp = "^$|^[\\d\\s()\\-+]{10,20}$",
             message = "Telefone deve conter 10-20 caracteres (DDD + número)")
    private String telefone;
}
