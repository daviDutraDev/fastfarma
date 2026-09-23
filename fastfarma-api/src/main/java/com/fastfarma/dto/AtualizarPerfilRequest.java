package com.fastfarma.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AtualizarPerfilRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String nome;

    @Pattern(regexp = "^$|^[\\d\\s()\\-+]{10,20}$",
             message = "Telefone deve conter 10-20 caracteres (DDD + número)")
    private String telefone;
}
