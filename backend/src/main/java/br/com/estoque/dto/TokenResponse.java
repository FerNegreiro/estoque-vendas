package br.com.estoque.dto;

public record TokenResponse(String token, String tipo, long expiraEmSegundos, UsuarioResponse usuario) {
}
