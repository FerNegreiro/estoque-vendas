package br.com.estoque.dto;

import java.math.BigDecimal;

public record ProdutoMaisVendidoResponse(Long produtoId, String nome, Long quantidade, BigDecimal total) {
}
