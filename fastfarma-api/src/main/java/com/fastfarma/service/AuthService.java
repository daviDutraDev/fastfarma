package com.fastfarma.service;

import com.fastfarma.dto.*;
import com.fastfarma.model.*;
import com.fastfarma.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;

    @PostConstruct
    @Transactional
    public void criarAdminPadrao() {
        if (usuarioRepository.count() == 0) {
            Usuario admin = Usuario.builder()
                    .nome("admin")
                    .email("admin@gmail.com")
                    .senha("admin")
                    .tipo(TipoUsuario.FUNCIONARIO)
                    .build();
            usuarioRepository.save(admin);
        }
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        return usuarioRepository.findByEmail(request.getEmail())
                .filter(u -> u.getSenha().equals(request.getSenha()))
                .map(u -> LoginResponse.builder()
                        .id(u.getId())
                        .nome(u.getNome())
                        .email(u.getEmail())
                        .tipo(u.getTipo())
                        .mensagem("Login realizado com sucesso!")
                        .build())
                .orElse(null);
    }

    @Transactional
    public UsuarioResponse cadastrar(CadastroRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email já cadastrado");
        }
        Usuario usuario = Usuario.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .senha(request.getSenha())
                .tipo(TipoUsuario.CLIENTE)
                .build();
        usuario = usuarioRepository.save(usuario);
        return toResponse(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Integer id) {
        return usuarioRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    private UsuarioResponse toResponse(Usuario u) {
        return UsuarioResponse.builder()
                .id(u.getId())
                .nome(u.getNome())
                .email(u.getEmail())
                .tipo(u.getTipo())
                .criadoEm(u.getCriadoEm())
                .build();
    }
}
