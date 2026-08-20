package src.controller.fastfarma.repository;

import src.controller.fastfarma.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Integer> {
    List<Produto> findByNomeContainingIgnoreCase(String nome);
    List<Produto> findByEstoqueGreaterThan(Integer estoque);
    List<Produto> findByEstoqueLessThan(Integer estoque);
}
