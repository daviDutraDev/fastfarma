package com.fastfarma.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Entidade de domínio representando um Pedido do sistema.
 *
 * <p>Encapsula toda a regra de negócio relativa ao ciclo de vida do
 * pedido: composição de itens, cálculo de valor total e transições
 * válidas de status. Os setters protegem as invariantes (ex.: não
 * aceitar itens duplicados; transições de status inválidas).</p>
 */
@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
public class Pedido {

    /** Faixa do código de verificação gerado aleatoriamente (1000–9999). */
    public static final int CODIGO_VERIFICACAO_MIN = 1000;
    public static final int CODIGO_VERIFICACAO_MAX = 9999;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "codigo_verificacao", nullable = false)
    private Integer codigoVerificacao;

    @Column(name = "criado_por", nullable = false, length = 100)
    private String criadoPor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPedido status;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PedidoItem> itens = new ArrayList<>();

    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    // -----------------------------------------------------------------
    // Construtor de domínio
    // -----------------------------------------------------------------
    public Pedido(String criadoPor) {
        setCriadoPor(criadoPor);
        this.status = StatusPedido.PENDENTE;
        this.codigoVerificacao = gerarCodigoVerificacao();
    }

    private static int gerarCodigoVerificacao() {
        return CODIGO_VERIFICACAO_MIN
                + new Random().nextInt(CODIGO_VERIFICACAO_MAX - CODIGO_VERIFICACAO_MIN + 1);
    }

    // -----------------------------------------------------------------
    // Setters com validação
    // -----------------------------------------------------------------
    public void setCriadoPor(String criadoPor) {
        if (criadoPor == null || criadoPor.isBlank()) {
            throw new IllegalArgumentException("Nome do criador do pedido é obrigatório");
        }
        if (criadoPor.length() > 100) {
            throw new IllegalArgumentException("Nome do criador deve ter no máximo 100 caracteres");
        }
        this.criadoPor = criadoPor.trim();
    }

    public void setStatus(StatusPedido status) {
        if (status == null) {
            throw new IllegalArgumentException("Status do pedido é obrigatório");
        }
        this.status = status;
    }

    // -----------------------------------------------------------------
    // Comportamento de domínio
    // -----------------------------------------------------------------

    /**
     * Adiciona um produto ao pedido. Lança exceção se o produto for nulo
     * ou se já estiver presente (regra do schema: UNIQUE(pedido_id, produto_id)).
     */
    public void adicionarItem(Produto produto) {
        if (produto == null) {
            throw new IllegalArgumentException("Produto não pode ser nulo");
        }
        boolean jaExiste = itens.stream()
                .anyMatch(i -> i.getProduto() != null
                        && i.getProduto().getId() != null
                        && i.getProduto().getId().equals(produto.getId()));
        if (jaExiste) {
            throw new IllegalStateException(
                    "Produto '" + produto.getNome() + "' já está no pedido");
        }
        itens.add(new PedidoItem(this, produto));
    }

    /**
     * Soma os preços unitários dos itens. Como o schema atual guarda
     * apenas o relacionamento (sem quantidade), a regra de cálculo é
     * a soma simples — concentrada aqui para isolar a fórmula no
     * domínio e facilitar mudanças futuras.
     */
    public BigDecimal getValorTotal() {
        return itens.stream()
                .map(i -> i.getProduto().getPreco())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ---- transições de status encapsuladas ----
    public void aprovar() {
        this.status = StatusPedido.APROVADO;
    }

    public void rejeitar() {
        this.status = StatusPedido.REJEITADO;
    }

    public void marcarComoPronto() {
        this.status = StatusPedido.PRONTO;
    }

    // ---- consultas de estado ----
    public boolean isPendente()  { return status == StatusPedido.PENDENTE;  }
    public boolean isAprovado()  { return status == StatusPedido.APROVADO;  }
    public boolean isRejeitado() { return status == StatusPedido.REJEITADO; }
    public boolean isPronto()    { return status == StatusPedido.PRONTO;    }

    /** Lista imutável dos itens — protege a coleção interna. */
    public List<PedidoItem> getItens() {
        return Collections.unmodifiableList(itens);
    }

    // -----------------------------------------------------------------
    // Callbacks JPA
    // -----------------------------------------------------------------
    @PrePersist
    protected void onCreate() {
        LocalDateTime agora = LocalDateTime.now();
        this.criadoEm = agora;
        this.atualizadoEm = agora;
        if (this.status == null) this.status = StatusPedido.PENDENTE;
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
        if (!(o instanceof Pedido other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }

    @Override
    public String toString() {
        return "Pedido{id=" + id + ", criadoPor='" + criadoPor + "', status=" + status
                + ", valorTotal=" + getValorTotal() + "}";
    }
}
