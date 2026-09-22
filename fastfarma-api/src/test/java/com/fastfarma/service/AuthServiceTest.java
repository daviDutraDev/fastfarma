package com.fastfarma.service;

import com.fastfarma.dto.CadastroRequest;
import com.fastfarma.dto.LoginRequest;
import com.fastfarma.dto.LoginResponse;
import com.fastfarma.dto.UsuarioResponse;
import com.fastfarma.model.TipoUsuario;
import com.fastfarma.model.Usuario;
import com.fastfarma.repository.UsuarioRepository;
import com.fastfarma.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private UsuarioRepository repo;
    private PasswordEncoder encoder;
    private JwtService jwt;
    private AuthService service;

    @BeforeEach
    void setup() {
        repo = mock(UsuarioRepository.class);
        encoder = mock(PasswordEncoder.class);
        jwt = mock(JwtService.class);
        service = new AuthService(repo, encoder, jwt);
        // Injeta manualmente o @Value
        try {
            var field = AuthService.class.getDeclaredField("jwtExpirationMinutes");
            field.setAccessible(true);
            field.setLong(service, 1440L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void login_com_credenciais_validas_deve_retornar_token() {
        String hash = "$2a$10$hash";
        Usuario u = new Usuario("Joao", "joao@x.com", hash, TipoUsuario.CLIENTE);
        when(repo.findByEmail("joao@x.com")).thenReturn(Optional.of(u));
        when(encoder.matches("123456", hash)).thenReturn(true);
        when(jwt.generateToken("Joao", null, "CLIENTE")).thenReturn("token.jwt.aqui");

        LoginResponse resp = service.login(new LoginRequest("JOAO@X.COM", "123456"));
        assertNotNull(resp);
        assertEquals("token.jwt.aqui", resp.getToken());
        assertEquals("Bearer", resp.getTokenType());
        assertEquals(86400L, resp.getExpiresInSeconds());
    }

    @Test
    void login_com_senha_errada_deve_retornar_null() {
        Usuario u = new Usuario("Joao", "joao@x.com", "$2a$10$hash", TipoUsuario.CLIENTE);
        when(repo.findByEmail("joao@x.com")).thenReturn(Optional.of(u));
        when(encoder.matches("errada", "$2a$10$hash")).thenReturn(false);

        LoginResponse resp = service.login(new LoginRequest("joao@x.com", "errada"));
        assertNull(resp);
        verify(jwt, never()).generateToken(anyString(), any(), anyString());
    }

    @Test
    void login_com_email_inexistente_deve_retornar_null() {
        when(repo.findByEmail("ninguem@x.com")).thenReturn(Optional.empty());

        LoginResponse resp = service.login(new LoginRequest("ninguem@x.com", "qualquer"));
        assertNull(resp);
    }

    @Test
    void cadastrar_deve_hashear_senha_e_salvar() {
        when(repo.existsByEmail("novo@x.com")).thenReturn(false);
        when(encoder.encode("123456")).thenReturn("$2a$10$novohash");
        when(repo.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        CadastroRequest req = new CadastroRequest();
        req.setNome("Novo");
        req.setEmail("NOVO@X.COM");
        req.setSenha("123456");

        UsuarioResponse resp = service.cadastrar(req);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(repo).save(captor.capture());
        Usuario salvo = captor.getValue();
        assertEquals("$2a$10$novohash", salvo.getSenha(),
                "Senha deve estar hasheada antes de salvar");
        assertEquals(TipoUsuario.CLIENTE, salvo.getTipo());
        assertNotNull(resp);
    }

    @Test
    void cadastrar_com_email_duplicado_deve_lancar() {
        when(repo.existsByEmail("dup@x.com")).thenReturn(true);

        CadastroRequest req = new CadastroRequest();
        req.setNome("X");
        req.setEmail("dup@x.com");
        req.setSenha("1234");

        assertThrows(RuntimeException.class, () -> service.cadastrar(req));
        verify(repo, never()).save(any());
    }

    @Test
    void cadastrar_com_telefone_deve_propagar() {
        when(repo.existsByEmail("novo@x.com")).thenReturn(false);
        when(encoder.encode(anyString())).thenReturn("$2a$10$h");
        when(repo.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        CadastroRequest req = new CadastroRequest();
        req.setNome("Novo");
        req.setEmail("novo@x.com");
        req.setSenha("1234");
        req.setTelefone("(47) 99999-9999");

        service.cadastrar(req);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(repo).save(captor.capture());
        assertEquals("47999999999", captor.getValue().getTelefone());
    }

    @Test
    void excluir_deve_proteger_admin_id_1() {
        when(repo.existsById(1)).thenReturn(true);
        assertThrows(RuntimeException.class, () -> service.excluir(1));
        verify(repo, never()).deleteById(any());
    }
}
