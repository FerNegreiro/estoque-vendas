package br.com.estoque.dto;

import br.com.estoque.domain.Categoria;

public record CategoriaResponse(Long id, String nome) {

    public static CategoriaResponse from(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNome());
    }
}
