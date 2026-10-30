package br.com.estoque.dto;

import java.math.BigDecimal;
import java.util.List;

public record DetalhamentoCategoriaResponse(Long categoriaId, String categoria, int quantidade,
                                            BigDecimal valorEstoque, List<DetalhamentoProdutoResponse> produtos) {
}
