package com.fastfarma.service;

import com.fastfarma.dto.CadastroRequest;
import com.fastfarma.dto.LoginRequest;
import com.fastfarma.dto.LoginResponse;
import com.fastfarma.dto.UsuarioResponse;
import com.fastfarma.model.TipoUsuario;
import com.fastfarma.model.Usuario;
import com.fastfarma.repository.UsuarioRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementação do contrato {@link IAuthService}.
 *
 * <p>Aplica a separação entre contrato e implementação. Regras que
 * dizem respeito ao próprio {@link Usuario} (validar tamanho de
 * senha, normalizar e-mail) ficam na entidade — aqui ficam só
 * orquestração e persistência.</p>
 */
@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    private final UsuarioRepository usuarioRepository;

    @PostConstruct
    @Transactional
    public void criarAdminPadrao() {
        if (usuarioRepository.count() == 0) {
            usuarioRepository.save(
                    new Usuario("admin", "admin@gmail.com", "admin", TipoUsuario.FUNCIONARIO));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        return usuarioRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .filter(u -> u.validarSenha(request.getSenha()))
                .map(u -> LoginResponse.builder()
                        .id(u.getId())
                        .nome(u.getNome())
                        .email(u.getEmail())
                        .tipo(u.getTipo())
                        .mensagem("Login realizado com sucesso!")
                        .build())
                .orElse(null);
    }

    @Override
    @Transactional
    public UsuarioResponse cadastrar(CadastroRequest request) {
        String emailNormalizado = request.getEmail().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new RuntimeException("Email já cadastrado");
        }
        Usuario usuario = new Usuario(
                request.getNome(),
                emailNormalizado,
                request.getSenha(),
                TipoUsuario.CLIENTE);
        return UsuarioResponse.de(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Integer id) {
        return usuarioRepository.findById(id)
                .map(UsuarioResponse::de)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
}
