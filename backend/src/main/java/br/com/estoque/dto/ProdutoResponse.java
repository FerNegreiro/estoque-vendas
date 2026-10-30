package br.com.estoque.dto;

import br.com.estoque.domain.Produto;
import java.math.BigDecimal;

public record ProdutoResponse(Long id, String sku, String nome, String descricao, Long categoriaId,
                              String categoriaNome, BigDecimal precoCusto, BigDecimal precoVenda,
                              int quantidadeEstoque, int estoqueMinimo, boolean estoqueBaixo) {

    public static ProdutoResponse from(Produto p) {
        return new ProdutoResponse(p.getId(), p.getSku(), p.getNome(), p.getDescricao(),
                p.getCategoria().getId(), p.getCategoria().getNome(), p.getPrecoCusto(), p.getPrecoVenda(),
                p.getQuantidadeEstoque(), p.getEstoqueMinimo(), p.isEstoqueBaixo());
    }
}
