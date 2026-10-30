package br.com.estoque.repository;

import java.math.BigDecimal;

public record VendaPorProduto(Long produtoId, Long quantidade, BigDecimal faturamento) {
}
