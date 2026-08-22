package com.fastfarma.service;

import com.fastfarma.dto.ProdutoRequest;
import com.fastfarma.dto.ProdutoResponse;
import com.fastfarma.model.Produto;
import com.fastfarma.repository.ProdutoRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço de aplicação para {@link Produto}.
 *
 * <p>Implementa a interface {@link IProdutoService} (princípio da
 * inversão de dependência). A lógica que depende do estado do
 * próprio produto (validar estoque, calcular situação) foi movida
 * para a entidade — aqui ficam apenas os casos de uso / orquestração
 * que cruzam o limite da transação.</p>
 */
@Service
@RequiredArgsConstructor
public class ProdutoService implements IProdutoService {

    private final ProdutoRepository produtoRepository;

    // -----------------------------------------------------------------
    // Seed inicial (executado uma vez quando o banco está vazio)
    // -----------------------------------------------------------------
    @PostConstruct
    @Transactional
    public void criarProdutosIniciais() {
        if (produtoRepository.count() == 0) {
            produtoRepository.save(new Produto("Dipirona",    new BigDecimal("10.50"), 20));
            produtoRepository.save(new Produto("Paracetamol", new BigDecimal("8.00"),  20));
            produtoRepository.save(new Produto("Vitamina C",  new BigDecimal("15.00"), 20));
        }
    }

    // -----------------------------------------------------------------
    // Casos de uso
    // -----------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public List<ProdutoResponse> listarTodos() {
        return produtoRepository.findAll().stream()
                .map(ProdutoResponse::de)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProdutoResponse> buscarPorNome(String nome) {
        return produtoRepository.findByNomeContainingIgnoreCase(nome).stream()
                .map(ProdutoResponse::de)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProdutoResponse> listarDisponiveis() {
        return produtoRepository.findByEstoqueGreaterThan(0).stream()
                .map(ProdutoResponse::de)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProdutoResponse> listarEsgotados() {
        return produtoRepository.findByEstoqueLessThan(1).stream()
                .map(ProdutoResponse::de)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProdutoResponse buscarPorId(Integer id) {
        return ProdutoResponse.de(getById(id));
    }

    @Override
    @Transactional
    public ProdutoResponse criar(ProdutoRequest request) {
        Produto produto = new Produto(request.getNome(), request.getPreco(), request.getEstoque());
        return ProdutoResponse.de(produtoRepository.save(produto));
    }

    @Override
    @Transactional
    public ProdutoResponse atualizar(Integer id, ProdutoRequest request) {
        Produto produto = getById(id);
        produto.setNome(request.getNome());
        produto.setPreco(request.getPreco());
        produto.setEstoque(request.getEstoque());
        return ProdutoResponse.de(produtoRepository.save(produto));
    }

    @Override
    @Transactional
    public void excluir(Integer id) {
        if (!produtoRepository.existsById(id)) {
            throw new RuntimeException("Produto não encontrado");
        }
        produtoRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ProdutoResponse adicionarEstoque(Integer id, Integer quantidade) {
        Produto produto = getById(id);
        produto.adicionarEstoque(quantidade);
        return ProdutoResponse.de(produtoRepository.save(produto));
    }

    /**
     * Reduz o estoque de um produto em 1 unidade. Usado na criação
     * de pedidos — a regra de "tem que ter estoque" vive na entidade.
     */
    @Override
    @Transactional
    public void baixarEstoque(Integer id) {
        Produto produto = getById(id);
        produto.reduzirEstoque(1);
        produtoRepository.save(produto);
    }

    /** Devolve 1 unidade de cada produto — usado em rejeição de pedido. */
    @Override
    @Transactional
    public void devolverEstoque(List<Integer> idsProdutos) {
        for (Integer id : idsProdutos) {
            produtoRepository.findById(id).ifPresent(p -> {
                p.devolverEstoque();
                produtoRepository.save(p);
            });
        }
    }

    // -----------------------------------------------------------------
    // Helper privado
    // -----------------------------------------------------------------
    private Produto getById(Integer id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
    }
}
