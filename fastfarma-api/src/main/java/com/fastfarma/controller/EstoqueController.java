package com.fastfarma.controller;

import com.fastfarma.dto.*;
import com.fastfarma.service.ProdutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/estoque")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EstoqueController {

    private final ProdutoService produtoService;

    // =============================================
    // PUT /api/estoque/adicionar/{id}
    // Adiciona quantidade ao estoque de um produto
    // =============================================
    // Body (JSON):
    // {
    //   "quantidade": 10
    // }
    // =============================================
    @PutMapping("/adicionar/{id}")
    public ResponseEntity<ApiResponse<ProdutoResponse>> adicionar(
            @PathVariable Integer id,
            @Valid @RequestBody AtualizarEstoqueRequest request) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(
                    "Estoque adicionado com sucesso",
                    produtoService.adicionarEstoque(id, request.getQuantidade())));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.erro(e.getMessage()));
        }
    }
}
