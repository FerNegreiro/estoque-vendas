package br.com.estoque.security;

import br.com.estoque.domain.Usuario;
import br.com.estoque.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String PREFIXO = "Bearer ";

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String cabecalho = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (cabecalho != null && cabecalho.startsWith(PREFIXO)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            jwtService.extrairEmail(cabecalho.substring(PREFIXO.length()))
                    .flatMap(usuarioRepository::findByEmailIgnoreCase)
                    .filter(Usuario::isAtivo)
                    .ifPresent(usuario -> autenticar(usuario, request));
        }
        chain.doFilter(request, response);
    }

    private void autenticar(Usuario usuario, HttpServletRequest request) {
        var autoridades = List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getPerfil().name()));
        var autenticacao = new UsernamePasswordAuthenticationToken(usuario, null, autoridades);
        autenticacao.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(autenticacao);
    }
}
