package com.fastfarma.service;

import com.fastfarma.dto.PedidoRequest;
import com.fastfarma.dto.PedidoResponse;
import com.fastfarma.model.Pedido;
import com.fastfarma.model.Produto;
import com.fastfarma.model.StatusPedido;
import com.fastfarma.repository.PedidoRepository;
import com.fastfarma.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final IProdutoService produtoService;

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
    public PedidoResponse criar(String nomeCliente, PedidoRequest request) {
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

        return PedidoResponse.de(pedidoRepository.save(pedido));
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
        return PedidoResponse.de(pedidoRepository.save(pedido));
    }

    // -----------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------
    private Pedido getById(Integer id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
    }
}
