package com.fastfarma.service;

import com.fastfarma.dto.*;
import com.fastfarma.model.*;
import com.fastfarma.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final ProdutoService produtoService;

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarTodos() {
        return pedidoRepository.findAll().stream()
                .sorted(Comparator.comparing(Pedido::getId).reversed())
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPorUsuario(String nome) {
        return pedidoRepository.findByCriadoPorOrderByIdDesc(nome)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPorStatus(StatusPedido status) {
        return pedidoRepository.findByStatus(status)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PedidoResponse buscarPorId(Integer id) {
        return pedidoRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
    }

    @Transactional
    public PedidoResponse criar(String nomeCliente, PedidoRequest request) {
        // Validar estoque disponível
        for (Integer idProd : request.getIdsProdutos()) {
            Produto prod = produtoRepository.findById(idProd)
                    .orElseThrow(() -> new RuntimeException("Produto ID " + idProd + " não encontrado"));
            if (prod.getEstoque() <= 0) {
                throw new RuntimeException("Produto '" + prod.getNome() + "' está esgotado");
            }
        }

        // Gerar código de verificação aleatório (1000-9999)
        int codigo = 1000 + new Random().nextInt(9000);

        // Criar pedido
        Pedido pedido = Pedido.builder()
                .codigoVerificacao(codigo)
                .criadoPor(nomeCliente)
                .status(StatusPedido.PENDENTE)
                .build();

        // Associar itens e baixar estoque
        for (Integer idProd : request.getIdsProdutos()) {
            Produto produto = produtoRepository.findById(idProd).get();
            pedido.adicionarItem(produto);
            produtoService.baixarEstoque(idProd);
        }

        pedido = pedidoRepository.save(pedido);
        return toResponse(pedido);
    }

    @Transactional
    public PedidoResponse atualizarStatus(Integer id, StatusPedido novoStatus) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        StatusPedido statusAnterior = pedido.getStatus();

        // Se estiver rejeitando e ainda não estava rejeitado → devolver estoque
        if (novoStatus == StatusPedido.REJEITADO && statusAnterior != StatusPedido.REJEITADO) {
            List<Integer> ids = pedido.getItens().stream()
                    .map(item -> item.getProduto().getId())
                    .collect(Collectors.toList());
            produtoService.devolverEstoque(ids);
        }

        pedido.setStatus(novoStatus);
        pedido = pedidoRepository.save(pedido);
        return toResponse(pedido);
    }

    private PedidoResponse toResponse(Pedido p) {
        BigDecimal total = p.getItens().stream()
                .map(item -> item.getProduto().getPreco())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<ItemPedidoResponse> itens = p.getItens().stream()
                .map(item -> ItemPedidoResponse.builder()
                        .produtoId(item.getProduto().getId())
                        .nome(item.getProduto().getNome())
                        .precoUnitario(item.getProduto().getPreco())
                        .build())
                .collect(Collectors.toList());

        return PedidoResponse.builder()
                .id(p.getId())
                .codigoVerificacao(p.getCodigoVerificacao())
                .criadoPor(p.getCriadoPor())
                .status(p.getStatus())
                .itens(itens)
                .valorTotal(total)
                .criadoEm(p.getCriadoEm())
                .atualizadoEm(p.getAtualizadoEm())
                .build();
    }
}
