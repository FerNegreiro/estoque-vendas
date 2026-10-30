package br.com.estoque.repository;

import java.math.BigDecimal;

public interface FaturamentoMensalView {

    Integer getMes();

    BigDecimal getFaturamento();

    Long getPedidos();
}
