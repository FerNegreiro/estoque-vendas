package br.com.estoque.dto;

import br.com.estoque.domain.MovimentacaoEstoque;
import br.com.estoque.domain.TipoMovimentacao;
import java.time.OffsetDateTime;

public record MovimentacaoResponse(Long id, Long produtoId, String produtoNome, TipoMovimentacao tipo,
                                   int quantidade, String motivo, String usuarioNome, Long vendaId,
                                   OffsetDateTime data) {

    public static MovimentacaoResponse from(MovimentacaoEstoque m) {
        Long vendaId = m.getVenda() == null ? null : m.getVenda().getId();
        return new MovimentacaoResponse(m.getId(), m.getProduto().getId(), m.getProduto().getNome(), m.getTipo(),
                m.getQuantidade(), m.getMotivo(), m.getUsuario().getNome(), vendaId, m.getDataMovimentacao());
    }
}
