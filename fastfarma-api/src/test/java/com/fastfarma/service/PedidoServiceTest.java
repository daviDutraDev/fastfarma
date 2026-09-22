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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PedidoServiceTest {

    private PedidoRepository pedidoRepo;
    private ProdutoRepository produtoRepo;
    private UsuarioRepository usuarioRepo;
    private IProdutoService produtoService;
    private NotificationService notifier;
    private PedidoService service;

    @BeforeEach
    void setup() {
        pedidoRepo = mock(PedidoRepository.class);
        produtoRepo = mock(ProdutoRepository.class);
        usuarioRepo = mock(UsuarioRepository.class);
        produtoService = mock(IProdutoService.class);
        notifier = mock(NotificationService.class);
        service = new PedidoService(pedidoRepo, produtoRepo, usuarioRepo, produtoService, notifier);
    }

    private static void setId(Object target, Object idValue) {
        try {
            Field f = target.getClass().getDeclaredField("id");
            f.setAccessible(true);
            f.set(target, idValue);
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private Produto prod(int id, String nome, int estoque) {
        Produto p = new Produto(nome, new BigDecimal("10.00"), estoque);
        setId(p, id);
        return p;
    }

    @Test
    void criar_deve_lancar_se_algum_produto_estiver_esgotado() {
        Produto a = prod(1, "A", 5);
        Produto b = prod(2, "B", 0);
        when(produtoRepo.findById(1)).thenReturn(Optional.of(a));
        when(produtoRepo.findById(2)).thenReturn(Optional.of(b));

        PedidoRequest req = new PedidoRequest();
        req.setIdsProdutos(List.of(1, 2));

        assertThrows(RuntimeException.class,
                () -> service.criar("cliente", req));
        verify(pedidoRepo, never()).save(any());
    }

    @Test
    void criar_com_produto_inexistente_deve_lancar() {
        when(produtoRepo.findById(99)).thenReturn(Optional.empty());

        PedidoRequest req = new PedidoRequest();
        req.setIdsProdutos(List.of(99));

        assertThrows(RuntimeException.class,
                () -> service.criar("cliente", req));
    }

    @Test
    void criar_com_sucesso_deve_baixar_estoque() {
        Produto a = prod(1, "A", 5);
        when(produtoRepo.findById(1)).thenReturn(Optional.of(a));
        when(pedidoRepo.save(any(Pedido.class))).thenAnswer(inv -> {
            Pedido p = inv.getArgument(0);
            setId(p, 100);
            return p;
        });

        PedidoRequest req = new PedidoRequest();
        req.setIdsProdutos(List.of(1));

        PedidoResponse resp = service.criar("cliente", req);
        assertNotNull(resp);
        verify(produtoService).baixarEstoque(1);
    }

    @Test
    void rejeitar_deve_devolver_estoque() {
        Pedido p = new Pedido("cliente");
        p.adicionarItem(prod(1, "A", 5));
        setId(p, 1);
        when(pedidoRepo.findById(1)).thenReturn(Optional.of(p));
        when(pedidoRepo.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        service.atualizarStatus(1, StatusPedido.REJEITADO);

        ArgumentCaptor<List<Integer>> ids = ArgumentCaptor.forClass(List.class);
        verify(produtoService).devolverEstoque(ids.capture());
        assertEquals(List.of(1), ids.getValue());
    }

    @Test
    void marcar_como_pronto_deve_enviar_whatsapp_quando_cliente_tem_telefone() {
        Pedido p = new Pedido("Joao");
        p.adicionarItem(prod(1, "A", 5));
        setId(p, 7);
        when(pedidoRepo.findById(7)).thenReturn(Optional.of(p));
        when(pedidoRepo.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario cliente = mock(Usuario.class);
        when(cliente.getNome()).thenReturn("Joao");
        when(cliente.getTelefone()).thenReturn("47999999999");
        when(usuarioRepo.findAll()).thenReturn(List.of(cliente));
        when(notifier.enviarWhatsApp(anyString(), anyString())).thenReturn(true);

        service.atualizarStatus(7, StatusPedido.PRONTO);

        ArgumentCaptor<String> msgCaptor = ArgumentCaptor.forClass(String.class);
        verify(notifier).enviarWhatsApp(eq("47999999999"), msgCaptor.capture());
        String msg = msgCaptor.getValue();
        assertTrue(msg.contains("Joao"));
        assertTrue(msg.contains("PRONTO"));
        assertTrue(msg.contains("#7"));
    }

    @Test
    void marcar_como_pronto_sem_telefone_nao_deve_enviar_whatsapp() {
        Pedido p = new Pedido("Anonimo");
        p.adicionarItem(prod(1, "A", 5));
        setId(p, 8);
        when(pedidoRepo.findById(8)).thenReturn(Optional.of(p));
        when(pedidoRepo.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario cliente = mock(Usuario.class);
        when(cliente.getNome()).thenReturn("Anonimo");
        when(cliente.getTelefone()).thenReturn(null);
        when(usuarioRepo.findAll()).thenReturn(List.of(cliente));

        service.atualizarStatus(8, StatusPedido.PRONTO);

        verify(notifier, never()).enviarWhatsApp(anyString(), anyString());
    }

    @Test
    void buscarPorId_deve_lancar_quando_nao_existir() {
        when(pedidoRepo.findById(99)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.buscarPorId(99));
    }
}
