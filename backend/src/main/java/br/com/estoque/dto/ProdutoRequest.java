package br.com.estoque.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProdutoRequest(
        @NotBlank @Size(max = 40)
        @Pattern(regexp = "^[A-Za-z0-9-]+$", message = "use apenas letras, números e hífen")
        String sku,
        @NotBlank @Size(max = 150) String nome,
        @Size(max = 500) String descricao,
        @NotNull Long categoriaId,
        @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal precoCusto,
        @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal precoVenda,
        @NotNull @Min(0) @Max(1_000_000) Integer estoqueMinimo,
        @Min(0) @Max(1_000_000) Integer quantidadeInicial) {
}
