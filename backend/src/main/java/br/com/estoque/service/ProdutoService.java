package br.com.estoque.service;

import br.com.estoque.domain.Categoria;
import br.com.estoque.domain.MovimentacaoEstoque;
import br.com.estoque.domain.Produto;
import br.com.estoque.domain.TipoMovimentacao;
import br.com.estoque.dto.ProdutoRequest;
import br.com.estoque.dto.ProdutoResponse;
import br.com.estoque.exception.NegocioException;
import br.com.estoque.exception.RecursoNaoEncontradoException;
import br.com.estoque.repository.CategoriaRepository;
import br.com.estoque.repository.MovimentacaoEstoqueRepository;
import br.com.estoque.repository.ProdutoRepository;
import br.com.estoque.repository.UsuarioRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public Page<ProdutoResponse> listar(String termo, Pageable pageable) {
        String filtro = termo == null ? "" : termo.trim();
        return produtoRepository.buscarAtivos(filtro, pageable).map(ProdutoResponse::from);
    }

    @Transactional(readOnly = true)
    public ProdutoResponse buscar(Long id) {
        return ProdutoResponse.from(obterAtivo(id));
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponse> estoqueBaixo() {
        return produtoRepository.buscarComEstoqueBaixo().stream().map(ProdutoResponse::from).toList();
    }

    @Transactional
    public ProdutoResponse criar(ProdutoRequest request, Long usuarioId) {
        if (produtoRepository.existsBySkuIgnoreCase(request.sku().trim())) {
            throw new NegocioException("Já existe um produto com o SKU " + request.sku().trim().toUpperCase());
        }
        Produto produto = new Produto();
        aplicarDados(produto, request);
        produtoRepository.save(produto);

        int quantidadeInicial = request.quantidadeInicial() == null ? 0 : request.quantidadeInicial();
        if (quantidadeInicial > 0) {
            produto.adicionarEstoque(quantidadeInicial);
            movimentacaoRepository.save(new MovimentacaoEstoque(produto, TipoMovimentacao.ENTRADA,
                    quantidadeInicial, "Estoque inicial", usuarioRepository.getReferenceById(usuarioId), null));
        }
        return ProdutoResponse.from(produto);
    }

    @Transactional
    public ProdutoResponse atualizar(Long id, ProdutoRequest request) {
        Produto produto = obterAtivo(id);
        if (produtoRepository.existsBySkuIgnoreCaseAndIdNot(request.sku().trim(), id)) {
            throw new NegocioException("Já existe outro produto com o SKU " + request.sku().trim().toUpperCase());
        }
        aplicarDados(produto, request);
        return ProdutoResponse.from(produto);
    }

    @Transactional
    public void desativar(Long id) {
        obterAtivo(id).setAtivo(false);
    }

    private Produto obterAtivo(Long id) {
        return produtoRepository.findComCategoriaById(id)
                .filter(Produto::isAtivo)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado"));
    }

    private void aplicarDados(Produto produto, ProdutoRequest request) {
        Categoria categoria = categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada"));
        produto.setSku(request.sku().trim().toUpperCase());
        produto.setNome(request.nome().trim());
        produto.setDescricao(request.descricao() == null ? null : request.descricao().trim());
        produto.setCategoria(categoria);
        produto.setPrecoCusto(request.precoCusto());
        produto.setPrecoVenda(request.precoVenda());
        produto.setEstoqueMinimo(request.estoqueMinimo());
    }
}
