package com.fastfarma.service;

import com.fastfarma.dto.CadastroRequest;
import com.fastfarma.dto.LoginRequest;
import com.fastfarma.dto.LoginResponse;
import com.fastfarma.dto.UsuarioResponse;
import com.fastfarma.model.TipoUsuario;
import com.fastfarma.model.Usuario;
import com.fastfarma.repository.UsuarioRepository;
import com.fastfarma.security.JwtService;
import com.fastfarma.security.PasswordEncoder;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementação do contrato {@link IAuthService}.
 *
 * <p>Senhas são armazenadas com BCrypt (PasswordEncoder). O login emite
 * um token JWT contendo o nome (subject), o id (uid) e a role.</p>
 */
@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${fastfarma.jwt.expiration-minutes:1440}")
    private long jwtExpirationMinutes;

    @PostConstruct
    @Transactional
    public void criarAdminPadrao() {
        if (usuarioRepository.count() == 0) {
            String hash = passwordEncoder.encode("admin");
            Usuario admin = new Usuario("admin", "admin@gmail.com", hash, TipoUsuario.FUNCIONARIO);
            usuarioRepository.save(admin);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        return usuarioRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .filter(u -> u.validarSenha(request.getSenha(), passwordEncoder))
                .map(u -> {
                    String token = jwtService.generateToken(
                            u.getNome(), u.getId(), u.getTipo().name());
                    return LoginResponse.builder()
                            .id(u.getId())
                            .nome(u.getNome())
                            .email(u.getEmail())
                            .telefone(u.getTelefone())
                            .tipo(u.getTipo())
                            .token(token)
                            .tokenType("Bearer")
                            .expiresInSeconds(jwtExpirationMinutes * 60)
                            .mensagem("Login realizado com sucesso!")
                            .build();
                })
                .orElse(null);
    }

    @Override
    @Transactional
    public UsuarioResponse cadastrar(CadastroRequest request) {
        String emailNormalizado = request.getEmail().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new RuntimeException("Email já cadastrado");
        }
        String hash = passwordEncoder.encode(request.getSenha());
        Usuario usuario = new Usuario(
                request.getNome(),
                emailNormalizado,
                hash,
                TipoUsuario.CLIENTE);
        if (request.getTelefone() != null && !request.getTelefone().isBlank()) {
            usuario.setTelefone(request.getTelefone());
        }
        return UsuarioResponse.de(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarTodos() {
        return usuarioRepository.findAll().stream()
                .sorted(Comparator.comparing(Usuario::getId))
                .map(UsuarioResponse::de)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Integer id) {
        return usuarioRepository.findById(id)
                .map(UsuarioResponse::de)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    @Override
    @Transactional
    public void excluir(Integer id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuário não encontrado");
        }
        // Protege o admin padrão: id 1 é o seed inicial
        if (id != null && id == 1) {
            throw new RuntimeException("Usuário administrador padrão não pode ser excluído");
        }
        usuarioRepository.deleteById(id);
    }
}
