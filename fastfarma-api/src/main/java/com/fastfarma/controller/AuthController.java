package com.fastfarma.controller;

import com.fastfarma.dto.ApiResponse;
import com.fastfarma.dto.AtualizarPerfilRequest;
import com.fastfarma.dto.CadastroRequest;
import com.fastfarma.dto.LoginRequest;
import com.fastfarma.dto.LoginResponse;
import com.fastfarma.dto.TrocarSenhaRequest;
import com.fastfarma.dto.UsuarioResponse;
import com.fastfarma.security.AuthPrincipal;
import com.fastfarma.service.IAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
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

    /** Retorna os dados do usuario autenticado. */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UsuarioResponse>> me() {
        String nome = AuthPrincipal.currentName();
        if (nome == null || nome.isBlank()) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.erro("Não autenticado."));
        }
        try {
            UsuarioResponse usuario = authService.buscarPorNome(nome);
            return ResponseEntity.ok(ApiResponse.ok("Perfil carregado", usuario));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(ApiResponse.erro(e.getMessage()));
        }
    }

    /** Atualiza nome e telefone do usuario autenticado. */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UsuarioResponse>> atualizarMe(
            @Valid @RequestBody AtualizarPerfilRequest request) {
        String nome = AuthPrincipal.currentName();
        if (nome == null || nome.isBlank()) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.erro("Não autenticado."));
        }
        try {
            UsuarioResponse usuario = authService.atualizarPerfil(nome, request);
            return ResponseEntity.ok(ApiResponse.ok("Perfil atualizado", usuario));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(ApiResponse.erro(e.getMessage()));
        }
    }

    /** Troca a senha do usuario autenticado (exige senha atual). */
    @PutMapping("/me/senha")
    public ResponseEntity<ApiResponse<Void>> trocarMinhaSenha(
            @Valid @RequestBody TrocarSenhaRequest request) {
        String nome = AuthPrincipal.currentName();
        if (nome == null || nome.isBlank()) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.erro("Não autenticado."));
        }
        try {
            authService.trocarSenha(nome, request);
            return ResponseEntity.ok(ApiResponse.ok("Senha alterada com sucesso"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(ApiResponse.erro(e.getMessage()));
        }
    }
}
