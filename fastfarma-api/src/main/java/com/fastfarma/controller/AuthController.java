package com.fastfarma.controller;

import com.fastfarma.dto.ApiResponse;
import com.fastfarma.dto.CadastroRequest;
import com.fastfarma.dto.LoginRequest;
import com.fastfarma.dto.LoginResponse;
import com.fastfarma.dto.UsuarioResponse;
import com.fastfarma.service.IAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    // Depende da abstração (interface), não da implementação concreta
    private final IAuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse result = authService.login(request);
        if (result == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.erro("Email ou senha incorretos"));
        }
        return ResponseEntity.ok(ApiResponse.ok("Login realizado com sucesso", result));
    }

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
