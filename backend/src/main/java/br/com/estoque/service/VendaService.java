package br.com.estoque.service;

import br.com.estoque.domain.ItemVenda;
import br.com.estoque.domain.MovimentacaoEstoque;
import br.com.estoque.domain.Produto;
import br.com.estoque.domain.TipoMovimentacao;
import br.com.estoque.domain.Usuario;
import br.com.estoque.domain.Venda;
import br.com.estoque.dto.ItemVendaRequest;
import br.com.estoque.dto.VendaRequest;
import br.com.estoque.dto.VendaResponse;
import br.com.estoque.dto.VendaResumoResponse;
import br.com.estoque.exception.NegocioException;
import br.com.estoque.exception.RecursoNaoEncontradoException;
import br.com.estoque.repository.MovimentacaoEstoqueRepository;
import br.com.estoque.repository.ProdutoRepository;
import br.com.estoque.repository.UsuarioRepository;
import br.com.estoque.repository.VendaRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VendaService {

    private final VendaRepository vendaRepository;
    private final ProdutoRepository produtoRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public VendaResponse registrar(VendaRequest request, Long usuarioId) {
        Usuario vendedor = usuarioRepository.getReferenceById(usuarioId);
        Map<Long, Integer> quantidades = consolidarItens(request.itens());
        Venda venda = vendaRepository.save(new Venda(vendedor));

        quantidades.forEach((produtoId, quantidade) -> {
            Produto produto = produtoRepository.buscarParaAtualizacao(produtoId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Produto " + produtoId + " não encontrado"));
            if (!produto.isAtivo()) {
                throw new NegocioException("O produto " + produto.getNome() + " está inativo");
            }
            produto.baixarEstoque(quantidade);
            venda.adicionarItem(produto, quantidade);
            movimentacaoRepository.save(new MovimentacaoEstoque(produto, TipoMovimentacao.SAIDA, quantidade,
                    "Venda #" + venda.getId(), vendedor, venda));
        });
        return VendaResponse.from(venda);
    }

    @Transactional
    public VendaResponse cancelar(Long vendaId, Long usuarioId) {
        Venda venda = vendaRepository.findById(vendaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Venda não encontrada"));
        venda.cancelar();
        Usuario responsavel = usuarioRepository.getReferenceById(usuarioId);

        venda.getItens().stream()
                .sorted(Comparator.comparing((ItemVenda item) -> item.getProduto().getId()))
                .forEach(item -> {
                    Produto produto = produtoRepository.buscarParaAtualizacao(item.getProduto().getId())
                            .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado"));
                    produto.adicionarEstoque(item.getQuantidade());
                    movimentacaoRepository.save(new MovimentacaoEstoque(produto, TipoMovimentacao.ENTRADA,
                            item.getQuantidade(), "Cancelamento da venda #" + venda.getId(), responsavel, venda));
                });
        return VendaResponse.from(venda);
    }

    @Transactional(readOnly = true)
    public Page<VendaResumoResponse> listar(Pageable pageable) {
        return vendaRepository.findAll(pageable).map(VendaResumoResponse::from);
    }

    @Transactional(readOnly = true)
    public VendaResponse buscar(Long id) {
        return vendaRepository.findDetalhadaById(id)
                .map(VendaResponse::from)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Venda não encontrada"));
    }

    private static Map<Long, Integer> consolidarItens(List<ItemVendaRequest> itens) {
        Map<Long, Integer> quantidades = new TreeMap<>();
        itens.forEach(item -> quantidades.merge(item.produtoId(), item.quantidade(), Integer::sum));
        return quantidades;
    }
}
