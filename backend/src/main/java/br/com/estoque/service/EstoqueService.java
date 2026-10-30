package br.com.estoque.service;

import br.com.estoque.domain.MovimentacaoEstoque;
import br.com.estoque.domain.Produto;
import br.com.estoque.domain.TipoMovimentacao;
import br.com.estoque.dto.MovimentacaoRequest;
import br.com.estoque.dto.MovimentacaoResponse;
import br.com.estoque.exception.RecursoNaoEncontradoException;
import br.com.estoque.repository.MovimentacaoEstoqueRepository;
import br.com.estoque.repository.ProdutoRepository;
import br.com.estoque.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EstoqueService {

    private final ProdutoRepository produtoRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public MovimentacaoResponse movimentar(MovimentacaoRequest request, Long usuarioId) {
        Produto produto = produtoRepository.buscarParaAtualizacao(request.produtoId())
                .filter(Produto::isAtivo)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado"));

        if (request.tipo() == TipoMovimentacao.ENTRADA) {
            produto.adicionarEstoque(request.quantidade());
        } else {
            produto.baixarEstoque(request.quantidade());
        }

        MovimentacaoEstoque movimentacao = movimentacaoRepository.save(new MovimentacaoEstoque(produto,
                request.tipo(), request.quantidade(), request.motivo().trim(),
                usuarioRepository.getReferenceById(usuarioId), null));
        return MovimentacaoResponse.from(movimentacao);
    }

    @Transactional(readOnly = true)
    public Page<MovimentacaoResponse> listar(Long produtoId, Pageable pageable) {
        Page<MovimentacaoEstoque> pagina = produtoId == null
                ? movimentacaoRepository.findAll(pageable)
                : movimentacaoRepository.findByProdutoId(produtoId, pageable);
        return pagina.map(MovimentacaoResponse::from);
    }
}
