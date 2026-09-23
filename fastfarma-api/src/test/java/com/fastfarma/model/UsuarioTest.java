package com.fastfarma.model;

import com.fastfarma.security.Pbkdf2PasswordEncoder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {

    private final Pbkdf2PasswordEncoder encoder = new Pbkdf2PasswordEncoder();

    @Test
    void construtor_deve_aceitar_hash_pbkdf2() {
        String hash = encoder.encode("123456");
        Usuario u = new Usuario("Joao", "joao@x.com", hash, TipoUsuario.CLIENTE);
        assertEquals("joao", u.getNome());
        assertEquals("joao@x.com", u.getEmail());
        assertTrue(u.isCliente());
        assertFalse(u.isFuncionario());
        assertTrue(u.validarSenha("123456", encoder));
        assertFalse(u.validarSenha("errado", encoder));
    }

    @Test
    void construtor_com_senha_nao_hasheada_deve_lancar() {
        assertThrows(IllegalArgumentException.class,
                () -> new Usuario("Joao", "joao@x.com", "123456", TipoUsuario.CLIENTE));
    }

    @Test
    void setSenha_com_hash_invalido_deve_lancar() {
        Usuario u = new Usuario("Joao", "joao@x.com", encoder.encode("ok"), TipoUsuario.CLIENTE);
        assertThrows(IllegalArgumentException.class, () -> u.setSenha("naoHasheado"));
    }

    @Test
    void setSenhaHasheada_deve_aceitar_apenas_pbkdf2() {
        Usuario u = new Usuario("Joao", "joao@x.com", encoder.encode("ok"), TipoUsuario.CLIENTE);
        assertThrows(IllegalArgumentException.class, () -> u.setSenhaHasheada("lixo"));
        assertThrows(IllegalArgumentException.class, () -> u.setSenhaHasheada("$2a$10$bcrypt"));
        assertDoesNotThrow(() -> u.setSenhaHasheada(encoder.encode("novaSenha")));
        assertTrue(u.validarSenha("novaSenha", encoder));
    }

    @Test
    void email_deve_ser_normalizado_para_lowercase() {
        Usuario u = new Usuario("Joao", "Joao@X.COM", encoder.encode("ok"), TipoUsuario.CLIENTE);
        assertEquals("joao@x.com", u.getEmail());
    }

    @Test
    void email_invalido_deve_lancar() {
        assertThrows(IllegalArgumentException.class,
                () -> new Usuario("Joao", "naoehemail", encoder.encode("ok"), TipoUsuario.CLIENTE));
    }

    @Test
    void nome_muito_curto_deve_lancar() {
        assertThrows(IllegalArgumentException.class,
                () -> new Usuario("A", "a@b.com", encoder.encode("ok"), TipoUsuario.CLIENTE));
    }

    @Test
    void telefone_deve_ser_normalizado_apenas_digitos() {
        Usuario u = new Usuario("Joao", "joao@x.com", encoder.encode("ok"), TipoUsuario.CLIENTE);
        u.setTelefone("(47) 99999-9999");
        assertEquals("47999999999", u.getTelefone());
    }

    @Test
    void telefone_com_poucos_digitos_deve_lancar() {
        Usuario u = new Usuario("Joao", "joao@x.com", encoder.encode("ok"), TipoUsuario.CLIENTE);
        assertThrows(IllegalArgumentException.class, () -> u.setTelefone("12345"));
    }

    @Test
    void telefone_vazio_deve_ser_null() {
        Usuario u = new Usuario("Joao", "joao@x.com", encoder.encode("ok"), TipoUsuario.CLIENTE);
        u.setTelefone("");
        assertNull(u.getTelefone());
    }

    @Test
    void trocarSenha_deve_validar_tamanho() {
        Usuario u = new Usuario("Joao", "joao@x.com", encoder.encode("ok"), TipoUsuario.CLIENTE);
        assertThrows(IllegalArgumentException.class, () -> u.trocarSenha("abc"));
        assertThrows(IllegalArgumentException.class, () -> u.trocarSenha(""));
    }

    @Test
    void isFuncionario_e_isCliente_devem_ser_mutuamente_exclusivos() {
        String hash = encoder.encode("x");
        Usuario cliente = new Usuario("C", "c@x.com", hash, TipoUsuario.CLIENTE);
        Usuario func    = new Usuario("F", "f@x.com", hash, TipoUsuario.FUNCIONARIO);
        assertTrue(cliente.isCliente());
        assertFalse(cliente.isFuncionario());
        assertTrue(func.isFuncionario());
        assertFalse(func.isCliente());
    }
}
