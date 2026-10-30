package br.com.estoque.dto;

import br.com.estoque.domain.StatusVenda;
import br.com.estoque.domain.Venda;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record VendaResponse(Long id, OffsetDateTime data, String vendedor, StatusVenda status,
                            BigDecimal valorTotal, List<ItemVendaResponse> itens) {

    public static VendaResponse from(Venda v) {
        return new VendaResponse(v.getId(), v.getDataVenda(), v.getUsuario().getNome(), v.getStatus(),
                v.getValorTotal(), v.getItens().stream().map(ItemVendaResponse::from).toList());
    }
}
