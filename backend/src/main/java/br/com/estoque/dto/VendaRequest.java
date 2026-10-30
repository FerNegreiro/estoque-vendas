package br.com.estoque.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record VendaRequest(@NotEmpty @Size(max = 100) List<@Valid ItemVendaRequest> itens) {
}
