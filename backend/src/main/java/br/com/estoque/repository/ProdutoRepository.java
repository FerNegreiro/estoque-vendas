package br.com.estoque.repository;

import br.com.estoque.domain.Produto;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    @EntityGraph(attributePaths = "categoria")
    @Query("""
            select p from Produto p
            where p.ativo = true
              and (lower(p.nome) like lower(concat('%', :termo, '%'))
                   or lower(p.sku) like lower(concat('%', :termo, '%')))
            """)
    Page<Produto> buscarAtivos(@Param("termo") String termo, Pageable pageable);

    @EntityGraph(attributePaths = "categoria")
    @Query("""
            select p from Produto p
            where p.ativo = true and p.quantidadeEstoque <= p.estoqueMinimo
            order by p.quantidadeEstoque asc
            """)
    List<Produto> buscarComEstoqueBaixo();

    @EntityGraph(attributePaths = "categoria")
    Optional<Produto> findComCategoriaById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Produto p where p.id = :id")
    Optional<Produto> buscarParaAtualizacao(@Param("id") Long id);

    @EntityGraph(attributePaths = "categoria")
    List<Produto> findByAtivoTrue();

    boolean existsBySkuIgnoreCase(String sku);

    boolean existsBySkuIgnoreCaseAndIdNot(String sku, Long id);
}
