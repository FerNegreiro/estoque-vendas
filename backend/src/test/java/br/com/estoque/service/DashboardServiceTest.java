package br.com.estoque.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.estoque.domain.Categoria;
import br.com.estoque.domain.ClasseAbc;
import br.com.estoque.domain.Produto;
import br.com.estoque.domain.StatusVenda;
import br.com.estoque.dto.DashboardResponse;
import br.com.estoque.exception.NegocioException;
import br.com.estoque.repository.FaturamentoMensalView;
import br.com.estoque.repository.ItemVendaRepository;
import br.com.estoque.repository.ProdutoRepository;
import br.com.estoque.repository.VendaPorProduto;
import br.com.estoque.repository.VendaRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    private static final String FUSO = "America/Sao_Paulo";
    private static final int ANO = Year.now(ZoneId.of(FUSO)).getValue();

    @Mock
    private VendaRepository vendaRepository;
    @Mock
    private ItemVendaRepository itemVendaRepository;
    @Mock
    private ProdutoRepository produtoRepository;

    private DashboardService dashboardService;
    private Produto arroz;
    private Produto feijao;
    private Produto sabonete;

    private record Mes(Integer getMes, BigDecimal getFaturamento, Long getPedidos) implements FaturamentoMensalView {
    }

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(vendaRepository, itemVendaRepository, produtoRepository, FUSO);
        Categoria alimentos = categoria(1L, "Alimentos");
        Categoria higiene = categoria(2L, "Higiene");
        arroz = produto(1L, "Arroz", alimentos, "20.00", 10);
        feijao = produto(2L, "Feijão", alimentos, "5.00", 4);
        sabonete = produto(3L, "Sabonete", higiene, "1.00", 0);
    }

    @Test
    void deveCalcularIndicadoresDoAno() {
        prepararCenario();

        DashboardResponse resposta = dashboardService.resumo(ANO, DashboardService.TODAS_AS_CATEGORIAS);

        assertThat(resposta.indicadores().faturamento()).isEqualByComparingTo("1500.00");
        assertThat(resposta.indicadores().variacaoPercentual()).isEqualByComparingTo("50.0");
        assertThat(resposta.indicadores().pedidos()).isEqualTo(30);
        assertThat(resposta.indicadores().valorEmEstoque()).isEqualByComparingTo("220.00");
        assertThat(resposta.indicadores().produtosZerados()).isEqualTo(1);
        assertThat(resposta.indicadores().rupturaPercentual()).isEqualByComparingTo("33.33");
        assertThat(resposta.indicadores().giroEstoque()).isEqualByComparingTo("1.36");
        assertThat(resposta.faturamentoMensal()).hasSize(12);
        assertThat(resposta.faturamentoMensal().get(2).faturamento()).isEqualByComparingTo("1000.00");
    }

    @Test
    void deveMontarRankingECurvaAbc() {
        prepararCenario();

        DashboardResponse resposta = dashboardService.resumo(ANO, DashboardService.TODAS_AS_CATEGORIAS);

        assertThat(resposta.maisVendidos()).extracting("nome").containsExactly("Feijão", "Arroz");
        assertThat(resposta.detalhamento()).extracting("categoria").containsExactly("Alimentos", "Higiene");
        assertThat(resposta.detalhamento().get(0).produtos())
                .extracting("nome", "classeAbc")
                .containsExactly(
                        tuple("Arroz", ClasseAbc.A),
                        tuple("Feijão", ClasseAbc.B));
    }

    @Test
    void filtroDeCategoriaRestringeIndicadoresMasNaoARosca() {
        prepararCenario();

        DashboardResponse resposta = dashboardService.resumo(ANO, 2L);

        assertThat(resposta.indicadores().produtosAtivos()).isEqualTo(1);
        assertThat(resposta.indicadores().rupturaPercentual()).isEqualByComparingTo("100.00");
        assertThat(resposta.detalhamento()).extracting("categoria").containsExactly("Higiene");
        assertThat(resposta.estoquePorCategoria()).hasSize(2);
    }

    @Test
    void noAnoCorrenteComparaComOMesmoPeriodoDoAnoAnterior() {
        prepararCenario();

        DashboardResponse resposta = dashboardService.resumo(ANO, DashboardService.TODAS_AS_CATEGORIAS);

        ArgumentCaptor<OffsetDateTime> fins = ArgumentCaptor.forClass(OffsetDateTime.class);
        verify(vendaRepository, times(2)).faturamentoPorMes(any(), fins.capture(), anyLong(), anyString());
        OffsetDateTime fimDaComparacao = fins.getAllValues().get(1);
        OffsetDateTime umAnoAtras = OffsetDateTime.now().minusYears(1);

        assertThat(resposta.indicadores().periodoParcial()).isTrue();
        assertThat(fimDaComparacao).isBetween(umAnoAtras.minusMinutes(1), umAnoAtras.plusMinutes(1));
    }

    @Test
    void emAnoEncerradoComparaComOAnoAnteriorInteiro() {
        prepararCenario();

        DashboardResponse resposta = dashboardService.resumo(ANO - 1, DashboardService.TODAS_AS_CATEGORIAS);

        ArgumentCaptor<OffsetDateTime> fins = ArgumentCaptor.forClass(OffsetDateTime.class);
        verify(vendaRepository, times(2)).faturamentoPorMes(any(), fins.capture(), anyLong(), anyString());
        OffsetDateTime fimDaComparacao = fins.getAllValues().get(1);

        assertThat(resposta.indicadores().periodoParcial()).isFalse();
        assertThat(fimDaComparacao.getYear()).isEqualTo(ANO - 1);
        assertThat(fimDaComparacao.getDayOfYear()).isEqualTo(1);
        assertThat(fimDaComparacao.getHour()).isZero();
    }

    @Test
    void deveRecusarAnoFuturo() {
        assertThatThrownBy(() -> dashboardService.resumo(ANO + 1, 0L))
                .isInstanceOf(NegocioException.class);
    }

    private void prepararCenario() {
        when(produtoRepository.findByAtivoTrue()).thenReturn(List.of(arroz, feijao, sabonete));
        when(itemVendaRepository.vendasPorProduto(eq(StatusVenda.CONCLUIDA), any(), any())).thenReturn(List.of(
                new VendaPorProduto(1L, 10L, new BigDecimal("1300.00")),
                new VendaPorProduto(2L, 20L, new BigDecimal("200.00"))));
        when(vendaRepository.faturamentoPorMes(any(), any(), anyLong(), anyString())).thenAnswer(chamada -> {
            OffsetDateTime inicio = chamada.getArgument(0);
            return inicio.getYear() == ANO
                    ? List.of(new Mes(3, new BigDecimal("1000.00"), 20L), new Mes(4, new BigDecimal("500.00"), 10L))
                    : List.of(new Mes(5, new BigDecimal("1000.00"), 25L));
        });
        when(vendaRepository.dataDaPrimeiraVenda()).thenReturn(Optional.empty());
    }

    private static Categoria categoria(Long id, String nome) {
        Categoria categoria = new Categoria(nome);
        categoria.setId(id);
        return categoria;
    }

    private static Produto produto(Long id, String nome, Categoria categoria, String custo, int saldo) {
        Produto produto = new Produto();
        produto.setId(id);
        produto.setNome(nome);
        produto.setSku("SKU-" + id);
        produto.setCategoria(categoria);
        produto.setPrecoCusto(new BigDecimal(custo));
        produto.setPrecoVenda(new BigDecimal(custo).multiply(BigDecimal.TWO));
        produto.setEstoqueMinimo(2);
        if (saldo > 0) {
            produto.adicionarEstoque(saldo);
        }
        return produto;
    }
}
