package com.fastfarma.controller;

import com.fastfarma.dto.ApiResponse;
import com.fastfarma.dto.UsuarioResponse;
import com.fastfarma.service.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final IAuthService authService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UsuarioResponse>>> listarTodos() {
        return ResponseEntity.ok(ApiResponse.ok("Lista de usuários", authService.listarTodos()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponse>> buscarPorId(@PathVariable Integer id) {
        try {
            UsuarioResponse usuario = authService.buscarPorId(id);
            return ResponseEntity.ok(ApiResponse.ok("Usuário encontrado", usuario));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.erro(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> excluir(@PathVariable Integer id) {
        try {
            authService.excluir(id);
            return ResponseEntity.ok(ApiResponse.ok("Usuário excluído com sucesso"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(ApiResponse.erro(e.getMessage()));
        }
    }
}
