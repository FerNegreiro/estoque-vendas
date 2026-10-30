package br.com.estoque.dto;

import br.com.estoque.domain.ClasseAbc;
import java.math.BigDecimal;

public record DetalhamentoProdutoResponse(Long produtoId, String sku, String nome, int quantidade,
                                          BigDecimal valorEstoque, BigDecimal faturamento,
                                          ClasseAbc classeAbc, boolean estoqueBaixo) {
}
