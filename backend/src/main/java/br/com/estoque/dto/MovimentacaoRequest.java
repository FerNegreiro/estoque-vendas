package br.com.estoque.dto;

import br.com.estoque.domain.TipoMovimentacao;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MovimentacaoRequest(
        @NotNull Long produtoId,
        @NotNull TipoMovimentacao tipo,
        @NotNull @Min(1) @Max(1_000_000) Integer quantidade,
        @NotBlank @Size(max = 200) String motivo) {
}
