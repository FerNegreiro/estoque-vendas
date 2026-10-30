package br.com.estoque.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public final class CurvaAbc {

    private static final BigDecimal LIMITE_A = new BigDecimal("0.80");
    private static final BigDecimal LIMITE_B = new BigDecimal("0.95");

    private CurvaAbc() {
    }

    public static Map<Long, ClasseAbc> classificar(Map<Long, BigDecimal> faturamentoPorProduto) {
        BigDecimal total = faturamentoPorProduto.values().stream()
                .filter(valor -> valor.signum() > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<Long, ClasseAbc> classes = new HashMap<>();
        BigDecimal acumulado = BigDecimal.ZERO;
        var ordenados = faturamentoPorProduto.entrySet().stream()
                .sorted(Map.Entry.<Long, BigDecimal>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .toList();

        for (var entrada : ordenados) {
            if (total.signum() == 0 || entrada.getValue().signum() <= 0) {
                classes.put(entrada.getKey(), ClasseAbc.C);
                continue;
            }
            BigDecimal participacaoAnterior = acumulado.divide(total, 6, RoundingMode.HALF_EVEN);
            classes.put(entrada.getKey(), classePara(participacaoAnterior));
            acumulado = acumulado.add(entrada.getValue());
        }
        return classes;
    }

    private static ClasseAbc classePara(BigDecimal participacaoAnterior) {
        if (participacaoAnterior.compareTo(LIMITE_A) < 0) {
            return ClasseAbc.A;
        }
        if (participacaoAnterior.compareTo(LIMITE_B) < 0) {
            return ClasseAbc.B;
        }
        return ClasseAbc.C;
    }
}
