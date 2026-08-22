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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoUsuario tipo;

    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    // -----------------------------------------------------------------
    // Construtor de domínio (valida invariantes na criação)
    // -----------------------------------------------------------------
    public Usuario(String nome, String email, String senha, TipoUsuario tipo) {
        setNome(nome);
        setEmail(email);
        setSenha(senha);
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
        this.senha = senha;
    }

    public void setTipo(TipoUsuario tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo de usuário é obrigatório");
        }
        this.tipo = tipo;
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
     * Compara a senha informada (em texto puro) com a senha armazenada.
     * <p>Nota: o projeto não usa hash — é uma comparação direta,
     * mantida aqui para isolar a regra "validar credencial" na
     * própria entidade, em vez de vazar para a camada de serviço.</p>
     */
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
