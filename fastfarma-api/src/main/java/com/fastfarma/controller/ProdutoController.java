package com.fastfarma.controller;

import com.fastfarma.dto.ApiResponse;
import com.fastfarma.dto.ProdutoRequest;
import com.fastfarma.dto.ProdutoResponse;
import com.fastfarma.service.IProdutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produtos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProdutoController {

    private final IProdutoService produtoService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProdutoResponse>>> listarTodos() {
        return ResponseEntity.ok(ApiResponse.ok("Lista de produtos", produtoService.listarTodos()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProdutoResponse>> buscarPorId(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(ApiResponse.ok("Produto encontrado", produtoService.buscarPorId(id)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(ApiResponse.erro(e.getMessage()));
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<List<ProdutoResponse>>> buscarPorNome(
            @RequestParam(required = false) String nome) {
        if (nome != null && !nome.isBlank()) {
            return ResponseEntity.ok(ApiResponse.ok("Produtos encontrados", produtoService.buscarPorNome(nome)));
        }
        return ResponseEntity.ok(ApiResponse.ok("Lista de produtos", produtoService.listarTodos()));
    }

    @GetMapping("/disponiveis")
    public ResponseEntity<ApiResponse<List<ProdutoResponse>>> listarDisponiveis() {
        return ResponseEntity.ok(ApiResponse.ok("Produtos disponíveis", produtoService.listarDisponiveis()));
    }

    @GetMapping("/esgotados")
    public ResponseEntity<ApiResponse<List<ProdutoResponse>>> listarEsgotados() {
        return ResponseEntity.ok(ApiResponse.ok("Produtos esgotados", produtoService.listarEsgotados()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProdutoResponse>> criar(@Valid @RequestBody ProdutoRequest request) {
        return ResponseEntity.status(201)
                .body(ApiResponse.ok("Produto criado com sucesso", produtoService.criar(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProdutoResponse>> atualizar(
            @PathVariable Integer id, @Valid @RequestBody ProdutoRequest request) {
        try {
            return ResponseEntity.ok(ApiResponse.ok("Produto atualizado com sucesso",
                    produtoService.atualizar(id, request)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(ApiResponse.erro(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> excluir(@PathVariable Integer id) {
        try {
            produtoService.excluir(id);
            return ResponseEntity.ok(ApiResponse.ok("Produto excluído com sucesso"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(ApiResponse.erro(e.getMessage()));
        }
    }
}
