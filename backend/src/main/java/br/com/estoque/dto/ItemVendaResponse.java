package br.com.estoque.dto;

import br.com.estoque.domain.ItemVenda;
import java.math.BigDecimal;

public record ItemVendaResponse(Long produtoId, String produtoNome, int quantidade, BigDecimal precoUnitario,
                                BigDecimal subtotal) {

    public static ItemVendaResponse from(ItemVenda item) {
        return new ItemVendaResponse(item.getProduto().getId(), item.getProduto().getNome(), item.getQuantidade(),
                item.getPrecoUnitario(), item.getSubtotal());
    }
}
