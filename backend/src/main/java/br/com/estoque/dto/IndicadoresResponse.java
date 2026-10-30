package br.com.estoque.dto;

import java.math.BigDecimal;

public record IndicadoresResponse(BigDecimal faturamento,
                                  BigDecimal faturamentoAnoAnterior,
                                  BigDecimal variacaoPercentual,
                                  boolean periodoParcial,
                                  long pedidos,
                                  BigDecimal valorEmEstoque,
                                  BigDecimal rupturaPercentual,
                                  long produtosZerados,
                                  long produtosAtivos,
                                  BigDecimal giroEstoque) {
}
