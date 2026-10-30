package br.com.estoque.dto;

import br.com.estoque.domain.StatusVenda;
import br.com.estoque.domain.Venda;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record VendaResumoResponse(Long id, OffsetDateTime data, String vendedor, StatusVenda status,
                                  BigDecimal valorTotal) {

    public static VendaResumoResponse from(Venda v) {
        return new VendaResumoResponse(v.getId(), v.getDataVenda(), v.getUsuario().getNome(), v.getStatus(),
                v.getValorTotal());
    }
}
