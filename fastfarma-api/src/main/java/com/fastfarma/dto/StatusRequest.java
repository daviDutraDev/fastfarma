package com.fastfarma.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StatusRequest {
    @NotBlank(message = "Status é obrigatório")
    private String status;
}
