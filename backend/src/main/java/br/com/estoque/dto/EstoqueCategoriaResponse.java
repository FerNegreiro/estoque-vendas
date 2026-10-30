package br.com.estoque.dto;

import java.math.BigDecimal;

public record EstoqueCategoriaResponse(Long categoriaId, String categoria, BigDecimal valor) {
}
