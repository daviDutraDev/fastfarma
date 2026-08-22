package com.fastfarma.controller;

import com.fastfarma.dto.ApiResponse;
import com.fastfarma.dto.UsuarioResponse;
import com.fastfarma.service.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final IAuthService authService;

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
}
