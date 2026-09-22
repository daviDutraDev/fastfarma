package com.fastfarma.service;

import com.fastfarma.dto.ProdutoRequest;
import com.fastfarma.dto.ProdutoResponse;
import com.fastfarma.model.Produto;
import com.fastfarma.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProdutoServiceTest {

    private ProdutoRepository repo;
    private ProdutoService service;

    @BeforeEach
    void setup() {
        repo = mock(ProdutoRepository.class);
        service = new ProdutoService(repo);
    }

    @Test
    void listarTodos_deve_mapear_entidade_para_response() {
        Produto p = new Produto("A", new BigDecimal("10.00"), 5);
        when(repo.findAll()).thenReturn(List.of(p));

        List<ProdutoResponse> out = service.listarTodos();
        assertEquals(1, out.size());
        assertEquals("A", out.get(0).getNome());
        assertEquals("Disponivel", out.get(0).getSituacao());
    }

    @Test
    void listarDisponiveis_deve_filtrar_por_estoque_positivo() {
        Produto a = new Produto("A", new BigDecimal("10.00"), 5);
        Produto b = new Produto("B", new BigDecimal("10.00"), 0);
        when(repo.findByEstoqueGreaterThan(0)).thenReturn(List.of(a));

        List<ProdutoResponse> out = service.listarDisponiveis();
        assertEquals(1, out.size());
        assertEquals("A", out.get(0).getNome());
    }

    @Test
    void listarEsgotados_deve_filtrar_por_estoque_zero() {
        Produto b = new Produto("B", new BigDecimal("10.00"), 0);
        when(repo.findByEstoqueLessThan(1)).thenReturn(List.of(b));

        List<ProdutoResponse> out = service.listarEsgotados();
        assertEquals(1, out.size());
        assertEquals("B", out.get(0).getNome());
    }

    @Test
    void criar_deve_passar_categoria_e_persistir() {
        when(repo.save(any(Produto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProdutoRequest req = new ProdutoRequest();
        req.setNome("Vitamina C");
        req.setPreco(new BigDecimal("15.00"));
        req.setEstoque(20);
        req.setCategoria("Suplemento");

        ProdutoResponse out = service.criar(req);
        assertEquals("Vitamina C", out.getNome());
        assertEquals("Suplemento", out.getCategoria());
    }

    @Test
    void buscarPorId_deve_lancar_quando_nao_existir() {
        when(repo.findById(99)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.buscarPorId(99));
    }

    @Test
    void atualizar_deve_somar_campos_e_persistir() {
        Produto p = new Produto("A", new BigDecimal("10.00"), 5);
        when(repo.findById(1)).thenReturn(Optional.of(p));
        when(repo.save(any(Produto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProdutoRequest req = new ProdutoRequest();
        req.setNome("A2");
        req.setPreco(new BigDecimal("11.00"));
        req.setEstoque(7);
        req.setCategoria("Analgésico");

        ProdutoResponse out = service.atualizar(1, req);
        assertEquals("A2", out.getNome());
        assertEquals("Analgésico", out.getCategoria());
        assertEquals(7, out.getEstoque());
    }

    @Test
    void excluir_deve_validar_existencia() {
        when(repo.existsById(1)).thenReturn(true);
        service.excluir(1);
        verify(repo).deleteById(1);
    }

    @Test
    void excluir_id_inexistente_deve_lancar() {
        when(repo.existsById(99)).thenReturn(false);
        assertThrows(RuntimeException.class, () -> service.excluir(99));
    }

    @Test
    void baixarEstoque_deve_chamar_reduzir_na_entidade() {
        Produto p = new Produto("A", new BigDecimal("10.00"), 5);
        when(repo.findById(1)).thenReturn(Optional.of(p));
        when(repo.save(any(Produto.class))).thenAnswer(inv -> inv.getArgument(0));

        service.baixarEstoque(1);
        assertEquals(4, p.getEstoque());
    }
}
