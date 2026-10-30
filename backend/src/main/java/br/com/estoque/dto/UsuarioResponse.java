package br.com.estoque.dto;

import br.com.estoque.domain.Perfil;
import br.com.estoque.domain.Usuario;

public record UsuarioResponse(Long id, String nome, String email, Perfil perfil, boolean ativo) {

    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(),
                usuario.getPerfil(), usuario.isAtivo());
    }
}
