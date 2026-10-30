package br.com.estoque.service;

import br.com.estoque.domain.ClasseAbc;
import br.com.estoque.domain.CurvaAbc;
import br.com.estoque.domain.Produto;
import br.com.estoque.domain.StatusVenda;
import br.com.estoque.dto.DashboardResponse;
import br.com.estoque.dto.DetalhamentoCategoriaResponse;
import br.com.estoque.dto.DetalhamentoProdutoResponse;
import br.com.estoque.dto.EstoqueCategoriaResponse;
import br.com.estoque.dto.FaturamentoMensalResponse;
import br.com.estoque.dto.IndicadoresResponse;
import br.com.estoque.dto.ProdutoMaisVendidoResponse;
import br.com.estoque.exception.NegocioException;
import br.com.estoque.repository.FaturamentoMensalView;
import br.com.estoque.repository.ItemVendaRepository;
import br.com.estoque.repository.ProdutoRepository;
import br.com.estoque.repository.VendaPorProduto;
import br.com.estoque.repository.VendaRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    public static final long TODAS_AS_CATEGORIAS = 0L;
    private static final int PRIMEIRO_ANO_ACEITO = 2000;
    private static final int TAMANHO_RANKING = 5;
    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    private final VendaRepository vendaRepository;
    private final ItemVendaRepository itemVendaRepository;
    private final ProdutoRepository produtoRepository;
    private final ZoneId fusoHorario;

    public DashboardService(VendaRepository vendaRepository, ItemVendaRepository itemVendaRepository,
                            ProdutoRepository produtoRepository,
                            @Value("${app.fuso-horario}") String fusoHorario) {
        this.vendaRepository = vendaRepository;
        this.itemVendaRepository = itemVendaRepository;
        this.produtoRepository = produtoRepository;
        this.fusoHorario = ZoneId.of(fusoHorario);
    }

    @Transactional(readOnly = true)
    public DashboardResponse resumo(Integer anoInformado, long categoriaId) {
        int anoAtual = Year.now(fusoHorario).getValue();
        int ano = anoInformado == null ? anoAtual : anoInformado;
        if (ano < PRIMEIRO_ANO_ACEITO || ano > anoAtual) {
            throw new NegocioException("Escolha um ano entre %d e %d".formatted(PRIMEIRO_ANO_ACEITO, anoAtual));
        }

        List<Produto> todosAtivos = produtoRepository.findByAtivoTrue();
        List<Produto> produtos = todosAtivos.stream()
                .filter(p -> categoriaId == TODAS_AS_CATEGORIAS || p.getCategoria().getId() == categoriaId)
                .toList();

        Map<Long, VendaPorProduto> vendas = itemVendaRepository
                .vendasPorProduto(StatusVenda.CONCLUIDA, inicioDoAno(ano), inicioDoAno(ano + 1)).stream()
                .collect(Collectors.toMap(VendaPorProduto::produtoId, Function.identity()));

        List<FaturamentoMensalResponse> mensal = faturamentoMensal(ano, categoriaId);
        boolean periodoParcial = ano == anoAtual;
        BigDecimal faturamentoAnoAnterior = faturamentoNoPeriodo(
                inicioDoAno(ano - 1), fimDaComparacao(ano, periodoParcial), categoriaId);

        Map<Long, ClasseAbc> classes = CurvaAbc.classificar(produtos.stream()
                .collect(Collectors.toMap(Produto::getId, p -> faturamentoDe(vendas, p))));

        return new DashboardResponse(
                ano,
                anosDisponiveis(anoAtual),
                categoriaId,
                indicadores(produtos, vendas, mensal, faturamentoAnoAnterior, periodoParcial),
                mensal,
                estoquePorCategoria(todosAtivos),
                maisVendidos(produtos, vendas),
                detalhamento(produtos, vendas, classes));
    }

    private IndicadoresResponse indicadores(List<Produto> produtos, Map<Long, VendaPorProduto> vendas,
                                            List<FaturamentoMensalResponse> mensal,
                                            BigDecimal faturamentoAnoAnterior, boolean periodoParcial) {
        BigDecimal faturamento = somarFaturamento(mensal);
        long pedidos = mensal.stream().mapToLong(FaturamentoMensalResponse::pedidos).sum();
        BigDecimal variacao = faturamentoAnoAnterior.signum() == 0
                ? null
                : faturamento.subtract(faturamentoAnoAnterior)
                        .multiply(CEM)
                        .divide(faturamentoAnoAnterior, 1, RoundingMode.HALF_EVEN);

        BigDecimal valorEmEstoque = produtos.stream().map(this::valorEmEstoque).reduce(BigDecimal.ZERO, BigDecimal::add);
        long zerados = produtos.stream().filter(p -> p.getQuantidadeEstoque() == 0).count();
        BigDecimal ruptura = produtos.isEmpty()
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(zerados).multiply(CEM)
                        .divide(BigDecimal.valueOf(produtos.size()), 2, RoundingMode.HALF_EVEN);

        BigDecimal custoDoVendido = produtos.stream()
                .map(p -> p.getPrecoCusto().multiply(BigDecimal.valueOf(quantidadeVendida(vendas, p))))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal giro = valorEmEstoque.signum() == 0
                ? BigDecimal.ZERO
                : custoDoVendido.divide(valorEmEstoque, 2, RoundingMode.HALF_EVEN);

        return new IndicadoresResponse(faturamento, faturamentoAnoAnterior, variacao, periodoParcial, pedidos,
                valorEmEstoque, ruptura, zerados, produtos.size(), giro);
    }

    private List<FaturamentoMensalResponse> faturamentoMensal(int ano, long categoriaId) {
        Map<Integer, FaturamentoMensalView> porMes = vendaRepository
                .faturamentoPorMes(inicioDoAno(ano), inicioDoAno(ano + 1), categoriaId, fusoHorario.getId())
                .stream()
                .collect(Collectors.toMap(FaturamentoMensalView::getMes, Function.identity()));

        return IntStream.rangeClosed(1, 12)
                .mapToObj(mes -> {
                    FaturamentoMensalView dados = porMes.get(mes);
                    return dados == null
                            ? new FaturamentoMensalResponse(mes, BigDecimal.ZERO, 0)
                            : new FaturamentoMensalResponse(mes, dados.getFaturamento(), dados.getPedidos());
                })
                .toList();
    }

    private BigDecimal faturamentoNoPeriodo(OffsetDateTime inicio, OffsetDateTime fim, long categoriaId) {
        return vendaRepository.faturamentoPorMes(inicio, fim, categoriaId, fusoHorario.getId()).stream()
                .map(FaturamentoMensalView::getFaturamento)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private OffsetDateTime fimDaComparacao(int ano, boolean periodoParcial) {
        return periodoParcial
                ? ZonedDateTime.now(fusoHorario).minusYears(1).toOffsetDateTime()
                : inicioDoAno(ano);
    }

    private List<EstoqueCategoriaResponse> estoquePorCategoria(List<Produto> produtos) {
        return produtos.stream()
                .collect(Collectors.groupingBy(p -> p.getCategoria().getId(), LinkedHashMap::new, Collectors.toList()))
                .values().stream()
                .map(doGrupo -> new EstoqueCategoriaResponse(
                        doGrupo.get(0).getCategoria().getId(),
                        doGrupo.get(0).getCategoria().getNome(),
                        doGrupo.stream().map(this::valorEmEstoque).reduce(BigDecimal.ZERO, BigDecimal::add)))
                .sorted(Comparator.comparing(EstoqueCategoriaResponse::valor).reversed())
                .toList();
    }

    private List<ProdutoMaisVendidoResponse> maisVendidos(List<Produto> produtos, Map<Long, VendaPorProduto> vendas) {
        return produtos.stream()
                .filter(p -> vendas.containsKey(p.getId()))
                .map(p -> new ProdutoMaisVendidoResponse(p.getId(), p.getNome(),
                        vendas.get(p.getId()).quantidade(), vendas.get(p.getId()).faturamento()))
                .sorted(Comparator.comparing(ProdutoMaisVendidoResponse::quantidade).reversed())
                .limit(TAMANHO_RANKING)
                .toList();
    }

    private List<DetalhamentoCategoriaResponse> detalhamento(List<Produto> produtos, Map<Long, VendaPorProduto> vendas,
                                                             Map<Long, ClasseAbc> classes) {
        return produtos.stream()
                .collect(Collectors.groupingBy(p -> p.getCategoria().getId(), LinkedHashMap::new, Collectors.toList()))
                .values().stream()
                .map(doGrupo -> {
                    List<DetalhamentoProdutoResponse> linhas = doGrupo.stream()
                            .map(p -> new DetalhamentoProdutoResponse(p.getId(), p.getSku(), p.getNome(),
                                    p.getQuantidadeEstoque(), valorEmEstoque(p), faturamentoDe(vendas, p),
                                    classes.get(p.getId()), p.isEstoqueBaixo()))
                            .sorted(Comparator.comparing(DetalhamentoProdutoResponse::valorEstoque).reversed())
                            .toList();
                    return new DetalhamentoCategoriaResponse(
                            doGrupo.get(0).getCategoria().getId(),
                            doGrupo.get(0).getCategoria().getNome(),
                            linhas.stream().mapToInt(DetalhamentoProdutoResponse::quantidade).sum(),
                            linhas.stream().map(DetalhamentoProdutoResponse::valorEstoque)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add),
                            linhas);
                })
                .sorted(Comparator.comparing(DetalhamentoCategoriaResponse::valorEstoque).reversed())
                .toList();
    }

    private List<Integer> anosDisponiveis(int anoAtual) {
        int primeiroAno = vendaRepository.dataDaPrimeiraVenda()
                .map(data -> Math.min(data.atZoneSameInstant(fusoHorario).getYear(), anoAtual))
                .orElse(anoAtual);
        return IntStream.iterate(anoAtual, ano -> ano >= primeiroAno, ano -> ano - 1).boxed().toList();
    }

    private OffsetDateTime inicioDoAno(int ano) {
        return Year.of(ano).atDay(1).atStartOfDay(fusoHorario).toOffsetDateTime();
    }

    private BigDecimal valorEmEstoque(Produto produto) {
        return produto.getPrecoCusto().multiply(BigDecimal.valueOf(produto.getQuantidadeEstoque()));
    }

    private static BigDecimal somarFaturamento(List<FaturamentoMensalResponse> mensal) {
        return mensal.stream().map(FaturamentoMensalResponse::faturamento).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal faturamentoDe(Map<Long, VendaPorProduto> vendas, Produto produto) {
        VendaPorProduto venda = vendas.get(produto.getId());
        return venda == null ? BigDecimal.ZERO : venda.faturamento();
    }

    private static long quantidadeVendida(Map<Long, VendaPorProduto> vendas, Produto produto) {
        VendaPorProduto venda = vendas.get(produto.getId());
        return venda == null ? 0 : venda.quantidade();
    }
}
