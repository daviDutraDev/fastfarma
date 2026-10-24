package com.fastfarma;

import com.fastfarma.model.Pedido;
import com.fastfarma.model.Produto;
import com.fastfarma.repository.PedidoRepository;
import com.fastfarma.service.RelatorioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {"spring.jpa.hibernate.ddl-auto=create-drop", "spring.sql.init.mode=never"}, showSql = false)
@Import(RelatorioService.class)
class RelatorioRepositoryTest {
    @Autowired PedidoRepository pedidos;
    @Autowired TestEntityManager em;
    @Autowired RelatorioService service;

    @Test void consultaRealDeveIncluirOsDoisDiasSemIncluirAMeiaNoiteSeguinte() {
        Produto produto = em.persistAndFlush(new Produto("Produto de teste", new BigDecimal("10.00"), 20));
        salvar(produto, "Antes", "2020-01-09T23:59:59");
        salvar(produto, "Início", "2020-01-10T00:00:00");
        salvar(produto, "Fim", "2020-01-11T23:59:59");
        salvar(produto, "Depois", "2020-01-12T00:00:00");
        em.clear();

        var encontrados = pedidos.findByCriadoEmGreaterThanEqualAndCriadoEmLessThan(
                LocalDateTime.parse("2020-01-10T00:00:00"), LocalDateTime.parse("2020-01-12T00:00:00"));
        assertEquals(2, encontrados.size());
        assertTrue(encontrados.stream().anyMatch(p -> p.getCriadoPor().equals("Início")));
        assertTrue(encontrados.stream().anyMatch(p -> p.getCriadoPor().equals("Fim")));
        var resumo = service.resumir(java.time.LocalDate.of(2020, 1, 10), java.time.LocalDate.of(2020, 1, 11), 30);
        assertEquals(2, resumo.validos());
        assertEquals(2, resumo.produtosPorQuantidade().getFirst().unidades());
    }

    private void salvar(Produto produto, String nome, String data) {
        Pedido p = new Pedido(nome); p.adicionarItem(produto); em.persistAndFlush(p);
        // O callback de criação usa a hora atual; fixa a data do cenário diretamente no H2 de teste.
        em.getEntityManager().createNativeQuery("update pedidos set criado_em = :data where id = :id")
                .setParameter("data", Timestamp.valueOf(LocalDateTime.parse(data)))
                .setParameter("id", p.getId()).executeUpdate();
    }
}
