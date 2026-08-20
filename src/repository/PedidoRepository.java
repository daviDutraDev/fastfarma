package src.controller.fastfarma.repository;

import src.controller.fastfarma.model.Pedido;
import src.controller.fastfarma.model.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Integer> {
    List<Pedido> findByCriadoPorOrderByIdDesc(String criadoPor);
    List<Pedido> findByStatus(StatusPedido status);
}
