package br.com.estoque.service;

import br.com.estoque.domain.Categoria;
import br.com.estoque.dto.CategoriaRequest;
import br.com.estoque.dto.CategoriaResponse;
import br.com.estoque.exception.NegocioException;
import br.com.estoque.repository.CategoriaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return categoriaRepository.findAll(Sort.by("nome")).stream().map(CategoriaResponse::from).toList();
    }

    @Transactional
    public CategoriaResponse criar(CategoriaRequest request) {
        String nome = request.nome().trim();
        if (categoriaRepository.existsByNomeIgnoreCase(nome)) {
            throw new NegocioException("Já existe a categoria " + nome);
        }
        return CategoriaResponse.from(categoriaRepository.save(new Categoria(nome)));
    }
}
