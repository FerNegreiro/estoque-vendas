package br.com.estoque.controller;

import br.com.estoque.domain.Usuario;
import br.com.estoque.dto.MovimentacaoRequest;
import br.com.estoque.dto.MovimentacaoResponse;
import br.com.estoque.dto.PaginaResponse;
import br.com.estoque.service.EstoqueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/estoque/movimentacoes")
@RequiredArgsConstructor
public class EstoqueController {

    private final EstoqueService estoqueService;

    @GetMapping
    public PaginaResponse<MovimentacaoResponse> listar(
            @RequestParam(required = false) Long produtoId,
            @PageableDefault(size = 20, sort = "dataMovimentacao", direction = Sort.Direction.DESC) Pageable pageable) {
        return PaginaResponse.de(estoqueService.listar(produtoId, pageable));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public MovimentacaoResponse movimentar(@Valid @RequestBody MovimentacaoRequest request,
                                           @AuthenticationPrincipal Usuario usuario) {
        return estoqueService.movimentar(request, usuario.getId());
    }
}
