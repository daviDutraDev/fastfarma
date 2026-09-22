package com.fastfarma.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidade de domínio representando um Usuário do sistema.
 *
 * <p>Aplica encapsulamento: todos os campos são {@code private} e as
 * invariantes (e-mail único, senha mínima, tipo válido) são garantidas
 * pelos setters e pelo construtor de domínio.</p>
 *
 * <p>Expõe comportamento de negócio como
 * {@link #isFuncionario()}, {@link #isCliente()} e
 * {@link #validarSenha(String)} — evita o modelo anêmico.</p>
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    public static final int TAMANHO_MINIMO_SENHA = 4;
    public static final int TAMANHO_MAXIMO_NOME = 100;
    public static final int TAMANHO_MAXIMO_EMAIL = 150;
    public static final int TAMANHO_TELEFONE = 20;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Column(length = TAMANHO_TELEFONE)
    private String telefone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoUsuario tipo;

    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    // -----------------------------------------------------------------
    // Construtor de domínio (valida invariantes na criação)
    // A senha passada aqui precisa estar JÁ HASHEADA — usar
    // setSenhaPlainForSeed(...) apenas em seeds/testes.
    // -----------------------------------------------------------------
    public Usuario(String nome, String email, String senhaHash, TipoUsuario tipo) {
        setNome(nome);
        setEmail(email);
        setSenhaHasheada(senhaHash);
        setTipo(tipo);
    }

    // -----------------------------------------------------------------
    // Setters com validação
    // -----------------------------------------------------------------
    public void setNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        String trimmed = nome.trim();
        if (trimmed.length() < 2 || trimmed.length() > TAMANHO_MAXIMO_NOME) {
            throw new IllegalArgumentException(
                    "Nome deve ter entre 2 e " + TAMANHO_MAXIMO_NOME + " caracteres");
        }
        this.nome = trimmed;
    }

    public void setEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email é obrigatório");
        }
        String trimmed = email.trim().toLowerCase();
        if (!trimmed.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Email inválido");
        }
        if (trimmed.length() > TAMANHO_MAXIMO_EMAIL) {
            throw new IllegalArgumentException(
                    "Email deve ter no máximo " + TAMANHO_MAXIMO_EMAIL + " caracteres");
        }
        this.email = trimmed;
    }

    public void setSenha(String senha) {
        if (senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("Senha é obrigatória");
        }
        if (senha.length() < TAMANHO_MINIMO_SENHA) {
            throw new IllegalArgumentException(
                    "Senha deve ter pelo menos " + TAMANHO_MINIMO_SENHA + " caracteres");
        }
        // Aceita tanto senha em texto puro quanto um hash BCrypt já pronto
        // (hashes BCrypt sempre começam com "$2a$" / "$2b$" / "$2y$").
        if (!(senha.startsWith("$2a$") || senha.startsWith("$2b$") || senha.startsWith("$2y$"))) {
            throw new IllegalArgumentException(
                    "Senha precisa estar hasheada (BCrypt). "
                            + "Use o PasswordEncoder antes de atribuir.");
        }
        this.senha = senha;
    }

    /**
     * Define a senha em texto puro — DEVE ser chamado pelo serviço
     * passando o hash gerado pelo BCryptPasswordEncoder. Centraliza
     * a regra "tamanho mínimo + hash" num único ponto.
     */
    public void setSenhaHasheada(String hash) {
        if (hash == null || hash.isBlank()) {
            throw new IllegalArgumentException("Senha é obrigatória");
        }
        if (!(hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$"))) {
            throw new IllegalArgumentException("Hash de senha inválido (esperado BCrypt).");
        }
        this.senha = hash;
    }

    /** Define a senha em texto puro para fins de seed/teste — bypassa o hash. */
    public void setSenhaPlainForSeed(String plain) {
        if (plain == null || plain.isBlank()) {
            throw new IllegalArgumentException("Senha é obrigatória");
        }
        if (plain.length() < TAMANHO_MINIMO_SENHA) {
            throw new IllegalArgumentException(
                    "Senha deve ter pelo menos " + TAMANHO_MINIMO_SENHA + " caracteres");
        }
        this.senha = plain;
    }

    public void setTipo(TipoUsuario tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo de usuário é obrigatório");
        }
        this.tipo = tipo;
    }

    /**
     * Define o telefone (somente dígitos, com DDD). Opcional —
     * usado para enviar notificações via WhatsApp quando o pedido
     * estiver pronto.
     */
    public void setTelefone(String telefone) {
        if (telefone == null || telefone.isBlank()) {
            this.telefone = null;
            return;
        }
        String digits = telefone.replaceAll("\\D", "");
        if (digits.length() < 10 || digits.length() > 11) {
            throw new IllegalArgumentException(
                    "Telefone deve ter 10 ou 11 dígitos (DDD + número)");
        }
        this.telefone = digits;
    }

    // -----------------------------------------------------------------
    // Comportamento de domínio
    // -----------------------------------------------------------------

    /** @return {@code true} se o usuário for do tipo {@code FUNCIONARIO}. */
    public boolean isFuncionario() {
        return tipo == TipoUsuario.FUNCIONARIO;
    }

    /** @return {@code true} se o usuário for do tipo {@code CLIENTE}. */
    public boolean isCliente() {
        return tipo == TipoUsuario.CLIENTE;
    }

    /**
     * Verifica credencial usando BCrypt — método preferido.
     * @param senhaInformada senha em texto puro vinda do request
     * @param encoder        BCryptPasswordEncoder injetado pelo Spring
     */
    public boolean validarSenha(String senhaInformada, org.springframework.security.crypto.password.PasswordEncoder encoder) {
        return senha != null && encoder.matches(senhaInformada, senha);
    }

    /** Compatibilidade legada: comparação direta (NÃO usar em produção). */
    @Deprecated
    public boolean validarSenha(String senhaInformada) {
        return senha != null && senha.equals(senhaInformada);
    }

    /** Atualiza a senha após validar o tamanho mínimo. */
    public void trocarSenha(String novaSenha) {
        setSenha(novaSenha);
    }

    // -----------------------------------------------------------------
    // Callbacks JPA
    // -----------------------------------------------------------------
    @PrePersist
    protected void onCreate() {
        LocalDateTime agora = LocalDateTime.now();
        this.criadoEm = agora;
        this.atualizadoEm = agora;
    }

    @PreUpdate
    protected void onUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }

    // -----------------------------------------------------------------
    // Identidade
    // -----------------------------------------------------------------
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Usuario{id=" + id + ", nome='" + nome + "', email='" + email
                + "', tipo=" + tipo + "}";
    }
}
