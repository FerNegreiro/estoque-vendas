package br.com.estoque.dto;

import java.math.BigDecimal;

public record FaturamentoMensalResponse(int mes, BigDecimal faturamento, long pedidos) {
}
