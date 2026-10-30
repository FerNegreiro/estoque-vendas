package br.com.estoque.repository;

import br.com.estoque.domain.ItemVenda;
import br.com.estoque.domain.StatusVenda;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemVendaRepository extends JpaRepository<ItemVenda, Long> {

    @Query("""
            select new br.com.estoque.repository.VendaPorProduto(
                       i.produto.id, sum(i.quantidade), sum(i.subtotal))
            from ItemVenda i
            where i.venda.status = :status
              and i.venda.dataVenda >= :inicio
              and i.venda.dataVenda < :fim
            group by i.produto.id
            """)
    List<VendaPorProduto> vendasPorProduto(@Param("status") StatusVenda status,
                                           @Param("inicio") OffsetDateTime inicio,
                                           @Param("fim") OffsetDateTime fim);
}
