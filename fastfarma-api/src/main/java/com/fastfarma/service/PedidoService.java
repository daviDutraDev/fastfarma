package com.fastfarma.service;

import com.fastfarma.dto.PedidoRequest;
import com.fastfarma.dto.PedidoResponse;
import com.fastfarma.model.Pedido;
import com.fastfarma.model.Produto;
import com.fastfarma.model.StatusPedido;
import com.fastfarma.model.Usuario;
import com.fastfarma.notifications.NotificationService;
import com.fastfarma.repository.PedidoRepository;
import com.fastfarma.repository.ProdutoRepository;
import com.fastfarma.repository.UsuarioRepository;
import com.fastfarma.security.AuthPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementação do contrato {@link IPedidoService}.
 *
 * <p>Regras de domínio (criar pedido, adicionar item, calcular
 * valor total, transições de status, validação de estoque) ficam
 * encapsuladas em {@link Pedido} e {@link Produto} — esta classe só
 * orquestra: persiste, lê, repassa.</p>
 */
@Service
@RequiredArgsConstructor
public class PedidoService implements IPedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;
    private final IProdutoService produtoService;
    private final NotificationService notificationService;

    // -----------------------------------------------------------------
    // Consultas
    // -----------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponse> listarTodos() {
        return pedidoRepository.findAll().stream()
                .sorted(Comparator.comparing(Pedido::getId).reversed())
                .map(PedidoResponse::de)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPorUsuario(String nome) {
        return pedidoRepository.findByCriadoPorOrderByIdDesc(nome).stream()
                .map(PedidoResponse::de)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPorStatus(StatusPedido status) {
        return pedidoRepository.findByStatus(status).stream()
                .map(PedidoResponse::de)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoResponse buscarPorId(Integer id) {
        return PedidoResponse.de(getById(id));
    }

    // -----------------------------------------------------------------
    // Comandos
    // -----------------------------------------------------------------
    @Override
    @Transactional
    public PedidoResponse criar(String nomeClienteHeader, PedidoRequest request) {
        // O nome do criador vem preferencialmente do JWT (AuthPrincipal),
        // mas mantemos o parametro para retro-compatibilidade com a
        // chamada existente do controller. O JWT sempre ganha.
        String nomeCliente = AuthPrincipal.currentName();
        if (nomeCliente == null || nomeCliente.isBlank()) {
            nomeCliente = nomeClienteHeader;
        }
        if (nomeCliente == null || nomeCliente.isBlank()) {
            throw new RuntimeException("Não foi possível identificar o cliente. Faça login.");
        }

        // 1) Validar que todos os produtos existem e têm estoque
        List<Produto> produtos = request.getIdsProdutos().stream()
                .map(id -> produtoRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Produto ID " + id + " não encontrado")))
                .toList();
        for (Produto p : produtos) {
            if (!p.temEstoque()) {
                throw new RuntimeException("Produto '" + p.getNome() + "' está esgotado");
            }
        }

        // 2) Construir o pedido via construtor de domínio (gera código,
        //    aplica invariantes e define status PENDENTE)
        Pedido pedido = new Pedido(nomeCliente);

        // 3) Adicionar itens (a regra de unicidade está no domínio)
        produtos.forEach(pedido::adicionarItem);

        // 4) Baixar estoque (regra na entidade) e persistir
        produtos.forEach(p -> produtoService.baixarEstoque(p.getId()));

        Pedido salvo = pedidoRepository.save(pedido);

        // 5) Notificacao WhatsApp IMEDIATA ao cliente com os medicamentos
        //    do pedido. Usa o telefone cadastrado no perfil (que o frontend
        //    acabou de atualizar via PUT /api/auth/me).
        agendarNotificacaoCriado(salvo);

        return PedidoResponse.de(salvo);
    }

    /**
     * Notifica o cliente por WhatsApp logo apos a criacao do pedido,
     * com a lista dos medicamentos. Se nao houver telefone cadastrado,
     * loga e segue sem erro (o cliente ainda vera o codigo de retirada
     * na tela).
     */
    private void agendarNotificacaoCriado(Pedido pedido) {
        Integer pedidoId = pedido.getId();
        String criadoPor = pedido.getCriadoPor();

        Runnable enviar = () -> {
            try {
                Usuario usuario = usuarioRepository.findByNomeIgnoreCase(criadoPor)
                        .orElse(null);
                if (usuario == null) {
                    System.err.println("[WhatsApp] Pedido #" + pedidoId
                            + ": cliente '" + criadoPor + "' nao encontrado.");
                    return;
                }
                String telefone = usuario.getTelefone();
                if (telefone == null || telefone.isBlank()) {
                    System.err.println("[WhatsApp] Pedido #" + pedidoId
                            + ": cliente '" + criadoPor
                            + "' nao tem telefone cadastrado.");
                    return;
                }

                StringBuilder sb = new StringBuilder();
                sb.append("Ola, ").append(usuario.getNome()).append("!\n\n")
                        .append("Seu pedido #").append(pedidoId)
                        .append(" foi recebido com sucesso.\n")
                        .append("Codigo de retirada: ")
                        .append(pedido.getCodigoVerificacao()).append("\n\n")
                        .append("Itens:\n");
                pedido.getItens().forEach(item -> {
                    String nome = item.getProduto() == null ? "?" : item.getProduto().getNome();
                    sb.append("- ").append(nome).append("\n");
                });
                sb.append("\nVoce recebera outra mensagem quando estiver pronto para retirada.");

                boolean ok = notificationService.enviarWhatsApp(telefone, sb.toString());
                System.out.println("[WhatsApp] CRIADO pedido #" + pedidoId
                        + " -> " + telefone + ": " + (ok ? "enviado" : "falhou"));
            } catch (Exception ex) {
                System.err.println("[WhatsApp] Falha ao enviar CRIADO pedido "
                        + pedidoId + ": " + ex.getMessage());
            }
        };

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            enviar.run();
                        }
                    });
        } else {
            enviar.run();
        }
    }

    @Override
    @Transactional
    public PedidoResponse atualizarStatus(Integer id, StatusPedido novoStatus) {
        Pedido pedido = getById(id);
        StatusPedido anterior = pedido.getStatus();

        if (novoStatus == StatusPedido.REJEITADO && anterior != StatusPedido.REJEITADO) {
            // Devolve o estoque dos produtos que estavam no pedido
            List<Integer> ids = pedido.getItens().stream()
                    .map(i -> i.getProduto().getId())
                    .toList();
            produtoService.devolverEstoque(ids);
        }

        // Transição encapsulada na entidade
        switch (novoStatus) {
            case APROVADO  -> pedido.aprovar();
            case REJEITADO -> pedido.rejeitar();
            case PRONTO    -> pedido.marcarComoPronto();
            case PENDENTE  -> pedido.setStatus(StatusPedido.PENDENTE);
        }
        Pedido salvo = pedidoRepository.save(pedido);

        // Notificação WhatsApp quando o pedido fica PRONTO.
        // Disparado APOS o commit via TransactionSynchronization para
        // garantir que so notifica se a transição foi persistida.
        if (novoStatus == StatusPedido.PRONTO && anterior != StatusPedido.PRONTO) {
            agendarNotificacaoPronto(salvo);
        }

        return PedidoResponse.de(salvo);
    }

    /**
     * Agenda o envio da mensagem de WhatsApp para depois do commit da
     * transação atual. Se não houver transação ativa, envia direto.
     */
    private void agendarNotificacaoPronto(Pedido pedido) {
        Integer pedidoId = pedido.getId();
        String criadoPor = pedido.getCriadoPor();

        Runnable enviar = () -> {
            try {
                Usuario usuario = usuarioRepository.findByNomeIgnoreCase(criadoPor)
                        .orElse(null);

                if (usuario == null) {
                    System.err.println("[WhatsApp] Pedido #" + pedidoId
                            + ": cliente '" + criadoPor + "' nao encontrado.");
                    return;
                }
                String telefone = usuario.getTelefone();
                if (telefone == null || telefone.isBlank()) {
                    System.err.println("[WhatsApp] Pedido #" + pedidoId
                            + ": cliente '" + criadoPor
                            + "' nao tem telefone cadastrado. Notificacao ignorada.");
                    return;
                }

                StringBuilder sb = new StringBuilder();
                sb.append("Ola, ").append(usuario.getNome()).append("! Seu pedido #")
                        .append(pedidoId).append(" (codigo de retirada ")
                        .append(pedido.getCodigoVerificacao()).append(") esta ")
                        .append("PRONTO para retirada na farmacia.\n\nItens:\n");
                pedido.getItens().forEach(item -> {
                    String nome = item.getProduto() == null ? "?" : item.getProduto().getNome();
                    sb.append("- ").append(nome).append("\n");
                });
                sb.append("\nFastFarma");

                boolean ok = notificationService.enviarWhatsApp(telefone, sb.toString());
                System.out.println("[WhatsApp] Pedido #" + pedidoId
                        + " -> " + telefone + ": " + (ok ? "enviado" : "falhou"));
            } catch (Exception ex) {
                // Nao propaga — a transacao ja foi commitada.
                System.err.println("[WhatsApp] Falha ao enviar para pedido "
                        + pedidoId + ": " + ex.getMessage());
            }
        };

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            enviar.run();
                        }
                    });
        } else {
            enviar.run();
        }
    }

    // -----------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------
    private Pedido getById(Integer id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
    }
}
