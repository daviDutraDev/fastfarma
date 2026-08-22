package com.fastfarma.controller;

import com.fastfarma.dto.*;
import com.fastfarma.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    // =============================================
    // POST /api/auth/login
    // =============================================
    // Body (JSON):
    // {
    //   "email": "admin@gmail.com",
    //   "senha": "admin"
    // }
    // =============================================
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse result = authService.login(request);
        if (result == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.erro("Email ou senha incorretos"));
        }
        return ResponseEntity.ok(ApiResponse.ok("Login realizado com sucesso", result));
    }

    // =============================================
    // POST /api/auth/cadastrar
    // =============================================
    // Body (JSON):
    // {
    //   "nome": "João Silva",
    //   "email": "joao@email.com",
    //   "senha": "123456"
    // }
    // =============================================
    @PostMapping("/cadastrar")
    public ResponseEntity<ApiResponse<UsuarioResponse>> cadastrar(@Valid @RequestBody CadastroRequest request) {
        try {
            UsuarioResponse usuario = authService.cadastrar(request);
            return ResponseEntity.status(201)
                    .body(ApiResponse.ok("Usuário cadastrado com sucesso", usuario));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.erro(e.getMessage()));
        }
    }
}
