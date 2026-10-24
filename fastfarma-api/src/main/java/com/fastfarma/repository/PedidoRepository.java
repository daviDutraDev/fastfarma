package com.fastfarma.repository;

import com.fastfarma.model.Pedido;
import com.fastfarma.model.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Integer> {
    List<Pedido> findByCriadoPorOrderByIdDesc(String criadoPor);
    List<Pedido> findByStatus(StatusPedido status);
    List<Pedido> findByCriadoEmGreaterThanEqualAndCriadoEmLessThan(LocalDateTime inicio, LocalDateTime fimExclusivo);
}
