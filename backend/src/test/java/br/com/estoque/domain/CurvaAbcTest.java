package br.com.estoque.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CurvaAbcTest {

    @Test
    void deveClassificarPelaParticipacaoAcumuladaNoFaturamento() {
        Map<Long, BigDecimal> faturamento = Map.of(
                1L, new BigDecimal("500"),
                2L, new BigDecimal("300"),
                3L, new BigDecimal("120"),
                4L, new BigDecimal("50"),
                5L, new BigDecimal("30"),
                6L, BigDecimal.ZERO);

        Map<Long, ClasseAbc> classes = CurvaAbc.classificar(faturamento);

        assertThat(classes).containsEntry(1L, ClasseAbc.A)
                .containsEntry(2L, ClasseAbc.A)
                .containsEntry(3L, ClasseAbc.B)
                .containsEntry(4L, ClasseAbc.B)
                .containsEntry(5L, ClasseAbc.C)
                .containsEntry(6L, ClasseAbc.C);
    }

    @Test
    void semVendasTodosOsProdutosFicamNaClasseC() {
        Map<Long, ClasseAbc> classes = CurvaAbc.classificar(Map.of(1L, BigDecimal.ZERO, 2L, BigDecimal.ZERO));

        assertThat(classes).containsEntry(1L, ClasseAbc.C).containsEntry(2L, ClasseAbc.C);
    }

    @Test
    void produtoUnicoComVendasEhClasseA() {
        assertThat(CurvaAbc.classificar(Map.of(7L, new BigDecimal("10")))).containsEntry(7L, ClasseAbc.A);
    }
}
