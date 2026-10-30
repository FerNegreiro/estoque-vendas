package br.com.estoque.controller;

import br.com.estoque.domain.Usuario;
import br.com.estoque.dto.PaginaResponse;
import br.com.estoque.dto.VendaRequest;
import br.com.estoque.dto.VendaResponse;
import br.com.estoque.dto.VendaResumoResponse;
import br.com.estoque.service.VendaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vendas")
@RequiredArgsConstructor
public class VendaController {

    private final VendaService vendaService;

    @GetMapping
    public PaginaResponse<VendaResumoResponse> listar(
            @PageableDefault(size = 20, sort = "dataVenda", direction = Sort.Direction.DESC) Pageable pageable) {
        return PaginaResponse.de(vendaService.listar(pageable));
    }

    @GetMapping("/{id}")
    public VendaResponse buscar(@PathVariable Long id) {
        return vendaService.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VendaResponse registrar(@Valid @RequestBody VendaRequest request,
                                   @AuthenticationPrincipal Usuario usuario) {
        return vendaService.registrar(request, usuario.getId());
    }

    @PostMapping("/{id}/cancelamento")
    @PreAuthorize("hasRole('ADMIN')")
    public VendaResponse cancelar(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        return vendaService.cancelar(id, usuario.getId());
    }
}
