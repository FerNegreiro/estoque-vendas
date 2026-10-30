package br.com.estoque.controller;

import br.com.estoque.dto.DashboardResponse;
import br.com.estoque.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public DashboardResponse resumo(@RequestParam(required = false) Integer ano,
                                    @RequestParam(defaultValue = "0") long categoriaId) {
        return dashboardService.resumo(ano, categoriaId);
    }
}
