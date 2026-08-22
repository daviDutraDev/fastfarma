package com.fastfarma.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidade de domínio representando um Produto do catálogo da farmácia.
 *
 * <p>Aplica encapsulamento: todos os campos são {@code private} e só podem
 * ser alterados através de métodos que validam as invariantes da classe
 * (ex.: preço deve ser positivo, estoque não pode ficar negativo).</p>
 *
 * <p>Possui comportamento de domínio além de dados, evitando o modelo
 * anêmico: {@link #temEstoque()}, {@link #reduzirEstoque(int)},
 * {@link #adicionarEstoque(int)} e {@link #getSituacao()}.</p>
 */
@Entity
@Table(name = "produtos")
@Getter
@Setter
@NoArgsConstructor
public class Produto {

    public static final int ESTOQUE_MINIMO_ALERTA = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(nullable = false)
    private Integer estoque;

    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    // -----------------------------------------------------------------
    // Construtor de domínio (usado na criação — sem id nem timestamps)
    // -----------------------------------------------------------------
    public Produto(String nome, BigDecimal preco, Integer estoque) {
        setNome(nome);
        setPreco(preco);
        setEstoque(estoque);
    }

    // -----------------------------------------------------------------
    // Setters com validação (mantêm as invariantes da classe)
    // -----------------------------------------------------------------
    public void setNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do produto é obrigatório");
        }
        if (nome.length() > 200) {
            throw new IllegalArgumentException("Nome deve ter no máximo 200 caracteres");
        }
        this.nome = nome;
    }

    public void setPreco(BigDecimal preco) {
        if (preco == null) {
            throw new IllegalArgumentException("Preço é obrigatório");
        }
        if (preco.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Preço deve ser maior que zero");
        }
        this.preco = preco;
    }

    public void setEstoque(Integer estoque) {
        if (estoque == null) {
            throw new IllegalArgumentException("Estoque é obrigatório");
        }
        if (estoque < 0) {
            throw new IllegalArgumentException("Estoque não pode ser negativo");
        }
        this.estoque = estoque;
    }

    // -----------------------------------------------------------------
    // Comportamento de domínio
    // -----------------------------------------------------------------

    /** Indica se há ao menos uma unidade disponível. */
    public boolean temEstoque() {
        return estoque != null && estoque > 0;
    }

    /**
     * Reduz o estoque em {@code quantidade} unidades.
     *
     * @throws IllegalStateException    se a quantidade solicitada exceder o estoque
     * @throws IllegalArgumentException se a quantidade for inválida
     */
    public void reduzirEstoque(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade a reduzir deve ser positiva");
        }
        if (!temEstoque() || this.estoque < quantidade) {
            throw new IllegalStateException(
                    "Estoque insuficiente para o produto '" + nome + "' (disponível: "
                            + estoque + ", solicitado: " + quantidade + ")");
        }
        this.estoque -= quantidade;
    }

    /**
     * Acrescenta {@code quantidade} unidades ao estoque.
     *
     * @throws IllegalArgumentException se a quantidade for inválida
     */
    public void adicionarEstoque(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade a adicionar deve ser positiva");
        }
        this.estoque += quantidade;
    }

    /** Devolve uma unidade ao estoque (usado em rejeição de pedidos). */
    public void devolverEstoque() {
        adicionarEstoque(1);
    }

    /** Indica se o estoque está abaixo do mínimo de alerta. */
    public boolean estoqueBaixo() {
        return estoque != null && estoque <= ESTOQUE_MINIMO_ALERTA;
    }

    /** @return {@code "Disponivel"} ou {@code "Esgotado"}. */
    public String getSituacao() {
        return temEstoque() ? "Disponivel" : "Esgotado";
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
    // Identidade / representações
    // -----------------------------------------------------------------
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Produto other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }

    @Override
    public String toString() {
        return "Produto{id=" + id + ", nome='" + nome + "', preco=" + preco
                + ", estoque=" + estoque + ", situacao=" + getSituacao() + "}";
    }
}
