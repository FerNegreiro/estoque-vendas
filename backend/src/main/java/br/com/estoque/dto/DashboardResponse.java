package br.com.estoque.dto;

import java.util.List;

public record DashboardResponse(int ano,
                                List<Integer> anosDisponiveis,
                                long categoriaId,
                                IndicadoresResponse indicadores,
                                List<FaturamentoMensalResponse> faturamentoMensal,
                                List<EstoqueCategoriaResponse> estoquePorCategoria,
                                List<ProdutoMaisVendidoResponse> maisVendidos,
                                List<DetalhamentoCategoriaResponse> detalhamento) {
}
