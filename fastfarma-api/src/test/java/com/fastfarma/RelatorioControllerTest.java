package com.fastfarma;

import com.fastfarma.controller.GlobalExceptionHandler;
import com.fastfarma.controller.RelatorioController;
import com.fastfarma.repository.PedidoRepository;
import com.fastfarma.repository.ProdutoRepository;
import com.fastfarma.security.AuthContext;
import com.fastfarma.security.SecurityBlockerFilter;
import com.fastfarma.service.RelatorioPdfService;
import com.fastfarma.service.RelatorioService;
import org.junit.jupiter.api.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RelatorioControllerTest {
    private MockMvc mvc;
    private PedidoRepository pedidos;

    @BeforeEach void preparar() {
        pedidos = mock(PedidoRepository.class);
        ProdutoRepository produtos = mock(ProdutoRepository.class);
        when(pedidos.findAll()).thenReturn(List.of()); when(produtos.findAll()).thenReturn(List.of());
        when(pedidos.findByCriadoEmGreaterThanEqualAndCriadoEmLessThan(any(), any())).thenReturn(List.of());
        mvc = MockMvcBuilders.standaloneSetup(new RelatorioController(pedidos, produtos,
                        new RelatorioService(pedidos, produtos), new RelatorioPdfService()))
                .setControllerAdvice(new GlobalExceptionHandler()).addFilters(new SecurityBlockerFilter()).build();
        AuthContext.set(new AuthContext.AuthUser("Funcionário", "FUNCIONARIO"));
    }
    @AfterEach void limpar() { AuthContext.clear(); }

    @Test void funcionarioPodeBaixarOsTresNovosPdfs() throws Exception {
        for (String tipo : List.of("desempenho", "geral", "reposicao")) {
            byte[] bytes = mvc.perform(get("/api/relatorios/" + tipo).param("inicio", "2026-01-01").param("fim", "2026-01-31"))
                    .andExpect(status().isOk()).andExpect(content().contentType("application/pdf"))
                    .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("relatorio-" + tipo + ".pdf")))
                    .andReturn().getResponse().getContentAsByteArray();
            assertEquals("%PDF", new String(bytes, 0, 4, java.nio.charset.StandardCharsets.US_ASCII));
        }
    }

    @Test void todoPeriodoDeveFuncionarSemDatas() throws Exception {
        mvc.perform(get("/api/relatorios/geral")).andExpect(status().isOk());
        verify(pedidos).findAll();
    }

    @Test void deveValidarDatasECoberturaNaApi() throws Exception {
        mvc.perform(get("/api/relatorios/desempenho").param("inicio", "2026-01-01")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/relatorios/geral").param("inicio", "2026-01-31").param("fim", "2026-01-01")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/relatorios/reposicao").param("cobertura", "0")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/relatorios/reposicao").param("cobertura", "366")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/relatorios/desempenho").param("inicio", "invalida").param("fim", "2026-01-31")).andExpect(status().isBadRequest());
    }

    @Test void filtrosAntigosContinuamFuncionandoComESemDatas() throws Exception {
        mvc.perform(get("/api/relatorios/pedidos")).andExpect(status().isOk());
        mvc.perform(get("/api/relatorios/pedidos/status").param("status", "PENDENTE")).andExpect(status().isOk());
        mvc.perform(get("/api/relatorios/produtos")).andExpect(status().isOk());
        mvc.perform(get("/api/relatorios/pedidos").param("inicio", "2026-01-01").param("fim", "2026-01-31")).andExpect(status().isOk());
        verify(pedidos).findByCriadoEmGreaterThanEqualAndCriadoEmLessThan(LocalDate.of(2026, 1, 1).atStartOfDay(), LocalDate.of(2026, 2, 1).atStartOfDay());
    }

    @Test void clienteENaoAutenticadoNaoPodemBaixarNovosRelatorios() throws Exception {
        for (String tipo : List.of("desempenho", "geral", "reposicao")) {
            AuthContext.set(new AuthContext.AuthUser("Cliente", "CLIENTE"));
            mvc.perform(get("/api/relatorios/" + tipo)).andExpect(status().isForbidden());
            AuthContext.clear();
            mvc.perform(get("/api/relatorios/" + tipo)).andExpect(status().isUnauthorized());
        }
        verifyNoInteractions(pedidos);
    }
}
