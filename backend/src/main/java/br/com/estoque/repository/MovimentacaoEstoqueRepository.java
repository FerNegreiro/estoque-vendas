package br.com.estoque.repository;

import br.com.estoque.domain.MovimentacaoEstoque;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {

    @Override
    @EntityGraph(attributePaths = {"produto", "usuario"})
    Page<MovimentacaoEstoque> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"produto", "usuario"})
    Page<MovimentacaoEstoque> findByProdutoId(Long produtoId, Pageable pageable);
}
