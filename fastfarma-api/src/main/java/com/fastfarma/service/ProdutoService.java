package com.fastfarma.service;

import com.fastfarma.dto.*;
import com.fastfarma.model.*;
import com.fastfarma.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    @PostConstruct
    @Transactional
    public void criarProdutosIniciais() {
        if (produtoRepository.count() == 0) {
            produtoRepository.save(Produto.builder()
                    .nome("Dipirona").preco(new BigDecimal("10.50")).estoque(20).build());
            produtoRepository.save(Produto.builder()
                    .nome("Paracetamol").preco(new BigDecimal("8.00")).estoque(20).build());
            produtoRepository.save(Produto.builder()
                    .nome("Vitamina C").preco(new BigDecimal("15.00")).estoque(20).build());
        }
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listarTodos() {
        return produtoRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponse> buscarPorNome(String nome) {
        return produtoRepository.findByNomeContainingIgnoreCase(nome)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listarDisponiveis() {
        return produtoRepository.findByEstoqueGreaterThan(0)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listarEsgotados() {
        return produtoRepository.findByEstoqueLessThan(1)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProdutoResponse buscarPorId(Integer id) {
        return produtoRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
    }

    @Transactional
    public ProdutoResponse criar(ProdutoRequest request) {
        Produto produto = Produto.builder()
                .nome(request.getNome())
                .preco(request.getPreco())
                .estoque(request.getEstoque())
                .build();
        produto = produtoRepository.save(produto);
        return toResponse(produto);
    }

    @Transactional
    public ProdutoResponse atualizar(Integer id, ProdutoRequest request) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
        produto.setNome(request.getNome());
        produto.setPreco(request.getPreco());
        produto.setEstoque(request.getEstoque());
        produto = produtoRepository.save(produto);
        return toResponse(produto);
    }

    @Transactional
    public void excluir(Integer id) {
        if (!produtoRepository.existsById(id)) {
            throw new RuntimeException("Produto não encontrado");
        }
        produtoRepository.deleteById(id);
    }

    @Transactional
    public ProdutoResponse adicionarEstoque(Integer id, Integer quantidade) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
        produto.setEstoque(produto.getEstoque() + quantidade);
        produto = produtoRepository.save(produto);
        return toResponse(produto);
    }

    @Transactional
    public void baixarEstoque(Integer id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
        if (produto.getEstoque() > 0) {
            produto.setEstoque(produto.getEstoque() - 1);
            produtoRepository.save(produto);
        }
    }

    @Transactional
    public void devolverEstoque(List<Integer> idsProdutos) {
        for (Integer id : idsProdutos) {
            produtoRepository.findById(id).ifPresent(produto -> {
                produto.setEstoque(produto.getEstoque() + 1);
                produtoRepository.save(produto);
            });
        }
    }

    private ProdutoResponse toResponse(Produto p) {
        return ProdutoResponse.builder()
                .id(p.getId())
                .nome(p.getNome())
                .preco(p.getPreco())
                .estoque(p.getEstoque())
                .situacao(p.getEstoque() > 0 ? "Disponivel" : "Esgotado")
                .criadoEm(p.getCriadoEm())
                .atualizadoEm(p.getAtualizadoEm())
                .build();
    }
}
