package br.com.estoque.service;

import br.com.estoque.domain.Usuario;
import br.com.estoque.dto.UsuarioRequest;
import br.com.estoque.dto.UsuarioResponse;
import br.com.estoque.exception.NegocioException;
import br.com.estoque.repository.UsuarioRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll(Sort.by("nome")).stream().map(UsuarioResponse::from).toList();
    }

    @Transactional
    public UsuarioResponse criar(UsuarioRequest request) {
        String email = request.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new NegocioException("Já existe um usuário com este e-mail");
        }
        Usuario usuario = new Usuario(request.nome().trim(), email,
                passwordEncoder.encode(request.senha()), request.perfil());
        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }
}
