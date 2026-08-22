package com.fastfarma.service;

import com.fastfarma.dto.ProdutoRequest;
import com.fastfarma.dto.ProdutoResponse;

import java.util.List;

/**
 * Contrato (interface) do serviço de produtos.
 *
 * <p>Definir a abstração aqui (em vez de acoplar o {@code Controller}
 * diretamente à implementação concreta) atende ao princípio da
 * inversão de dependência: facilita troca por mock em testes, e
 * deixa claro quais operações o domínio expõe para a camada HTTP.</p>
 */
public interface IProdutoService {

    List<ProdutoResponse> listarTodos();

    List<ProdutoResponse> buscarPorNome(String nome);

    List<ProdutoResponse> listarDisponiveis();

    List<ProdutoResponse> listarEsgotados();

    ProdutoResponse buscarPorId(Integer id);

    ProdutoResponse criar(ProdutoRequest request);

    ProdutoResponse atualizar(Integer id, ProdutoRequest request);

    void excluir(Integer id);

    ProdutoResponse adicionarEstoque(Integer id, Integer quantidade);

    /** Decrementa o estoque do produto em 1 unidade. */
    void baixarEstoque(Integer id);

    /** Devolve 1 unidade ao estoque de cada produto da lista. */
    void devolverEstoque(List<Integer> idsProdutos);
}
