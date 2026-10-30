package br.com.estoque.repository;

import br.com.estoque.domain.Venda;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VendaRepository extends JpaRepository<Venda, Long> {

    @Override
    @EntityGraph(attributePaths = "usuario")
    Page<Venda> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"usuario", "itens", "itens.produto"})
    Optional<Venda> findDetalhadaById(Long id);

    @Query("select min(v.dataVenda) from Venda v")
    Optional<OffsetDateTime> dataDaPrimeiraVenda();

    @Query(value = """
            select cast(extract(month from timezone(cast(:fuso as text), v.data_venda)) as integer) as mes,
                   sum(i.subtotal) as faturamento,
                   count(distinct v.id) as pedidos
            from vendas v
            join itens_venda i on i.venda_id = v.id
            join produtos p on p.id = i.produto_id
            where v.status = 'CONCLUIDA'
              and v.data_venda >= :inicio
              and v.data_venda < :fim
              and (:categoriaId = 0 or p.categoria_id = :categoriaId)
            group by 1
            """, nativeQuery = true)
    List<FaturamentoMensalView> faturamentoPorMes(@Param("inicio") OffsetDateTime inicio,
                                                  @Param("fim") OffsetDateTime fim,
                                                  @Param("categoriaId") long categoriaId,
                                                  @Param("fuso") String fuso);
}
