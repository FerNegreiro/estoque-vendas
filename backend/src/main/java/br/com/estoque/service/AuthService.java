package br.com.estoque.service;

import br.com.estoque.domain.Usuario;
import br.com.estoque.dto.LoginRequest;
import br.com.estoque.dto.TokenResponse;
import br.com.estoque.dto.UsuarioResponse;
import br.com.estoque.repository.UsuarioRepository;
import br.com.estoque.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(request.email().trim())
                .filter(Usuario::isAtivo)
                .filter(u -> passwordEncoder.matches(request.senha(), u.getSenhaHash()))
                .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas"));

        return new TokenResponse(jwtService.gerarToken(usuario), "Bearer",
                jwtService.getExpiracaoSegundos(), UsuarioResponse.from(usuario));
    }
}
