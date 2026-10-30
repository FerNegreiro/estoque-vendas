package br.com.estoque.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.estoque.domain.MovimentacaoEstoque;
import br.com.estoque.domain.Perfil;
import br.com.estoque.domain.Produto;
import br.com.estoque.domain.StatusVenda;
import br.com.estoque.domain.Usuario;
import br.com.estoque.domain.Venda;
import br.com.estoque.dto.ItemVendaRequest;
import br.com.estoque.dto.VendaRequest;
import br.com.estoque.dto.VendaResponse;
import br.com.estoque.exception.NegocioException;
import br.com.estoque.repository.MovimentacaoEstoqueRepository;
import br.com.estoque.repository.ProdutoRepository;
import br.com.estoque.repository.UsuarioRepository;
import br.com.estoque.repository.VendaRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VendaServiceTest {

    private static final long ID_VENDEDOR = 10L;

    @Mock
    private VendaRepository vendaRepository;
    @Mock
    private ProdutoRepository produtoRepository;
    @Mock
    private MovimentacaoEstoqueRepository movimentacaoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private VendaService vendaService;

    private Usuario vendedor;

    @BeforeEach
    void setUp() {
        vendedor = new Usuario("Maria Vendedora", "maria@loja.com", "hash", Perfil.VENDEDOR);
        vendedor.setId(ID_VENDEDOR);
    }

    @Test
    void deveRegistrarVendaCalculandoTotalEBaixandoEstoque() {
        prepararGravacaoDaVenda();
        Produto arroz = produto(1L, "Arroz 5 kg", "27.90", 10);
        Produto feijao = produto(2L, "Feijão 1 kg", "8.99", 5);
        when(produtoRepository.buscarParaAtualizacao(1L)).thenReturn(Optional.of(arroz));
        when(produtoRepository.buscarParaAtualizacao(2L)).thenReturn(Optional.of(feijao));

        VendaResponse resposta = vendaService.registrar(pedido(item(1L, 2), item(2L, 3)), ID_VENDEDOR);

        assertThat(resposta.valorTotal()).isEqualByComparingTo("82.77");
        assertThat(resposta.status()).isEqualTo(StatusVenda.CONCLUIDA);
        assertThat(arroz.getQuantidadeEstoque()).isEqualTo(8);
        assertThat(feijao.getQuantidadeEstoque()).isEqualTo(2);
        verify(movimentacaoRepository, times(2)).save(any(MovimentacaoEstoque.class));
    }

    @Test
    void deveJuntarLinhasRepetidasDoMesmoProduto() {
        prepararGravacaoDaVenda();
        Produto arroz = produto(1L, "Arroz 5 kg", "27.90", 10);
        when(produtoRepository.buscarParaAtualizacao(1L)).thenReturn(Optional.of(arroz));

        VendaResponse resposta = vendaService.registrar(pedido(item(1L, 2), item(1L, 3)), ID_VENDEDOR);

        assertThat(resposta.itens()).hasSize(1);
        assertThat(resposta.itens().get(0).quantidade()).isEqualTo(5);
        assertThat(arroz.getQuantidadeEstoque()).isEqualTo(5);
        verify(produtoRepository, times(1)).buscarParaAtualizacao(1L);
    }

    @Test
    void deveRecusarVendaComEstoqueInsuficiente() {
        prepararGravacaoDaVenda();
        Produto feijao = produto(2L, "Feijão 1 kg", "8.99", 1);
        when(produtoRepository.buscarParaAtualizacao(2L)).thenReturn(Optional.of(feijao));

        assertThatThrownBy(() -> vendaService.registrar(pedido(item(2L, 2)), ID_VENDEDOR))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("Estoque insuficiente");
        assertThat(feijao.getQuantidadeEstoque()).isEqualTo(1);
        verify(movimentacaoRepository, never()).save(any());
    }

    @Test
    void deveRecusarProdutoInativo() {
        prepararGravacaoDaVenda();
        Produto descontinuado = produto(3L, "Produto antigo", "5.00", 10);
        descontinuado.setAtivo(false);
        when(produtoRepository.buscarParaAtualizacao(3L)).thenReturn(Optional.of(descontinuado));

        assertThatThrownBy(() -> vendaService.registrar(pedido(item(3L, 1)), ID_VENDEDOR))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("inativo");
    }

    @Test
    void deveDevolverEstoqueAoCancelarVenda() {
        Produto arroz = produto(1L, "Arroz 5 kg", "27.90", 10);
        Venda venda = new Venda(vendedor);
        venda.setId(1L);
        venda.adicionarItem(arroz, 4);
        arroz.baixarEstoque(4);
        when(vendaRepository.findById(1L)).thenReturn(Optional.of(venda));
        when(usuarioRepository.getReferenceById(ID_VENDEDOR)).thenReturn(vendedor);
        when(produtoRepository.buscarParaAtualizacao(1L)).thenReturn(Optional.of(arroz));

        VendaResponse resposta = vendaService.cancelar(1L, ID_VENDEDOR);

        assertThat(resposta.status()).isEqualTo(StatusVenda.CANCELADA);
        assertThat(arroz.getQuantidadeEstoque()).isEqualTo(10);
        verify(movimentacaoRepository).save(any(MovimentacaoEstoque.class));
    }

    @Test
    void naoDeveCancelarVendaJaCancelada() {
        Venda venda = new Venda(vendedor);
        venda.setId(1L);
        venda.cancelar();
        when(vendaRepository.findById(1L)).thenReturn(Optional.of(venda));

        assertThatThrownBy(() -> vendaService.cancelar(1L, ID_VENDEDOR))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("já foi cancelada");
        verify(produtoRepository, never()).buscarParaAtualizacao(any());
    }

    private void prepararGravacaoDaVenda() {
        when(usuarioRepository.getReferenceById(ID_VENDEDOR)).thenReturn(vendedor);
        when(vendaRepository.save(any(Venda.class))).thenAnswer(chamada -> {
            Venda venda = chamada.getArgument(0);
            venda.setId(1L);
            return venda;
        });
    }

    private static Produto produto(Long id, String nome, String preco, int saldo) {
        Produto produto = new Produto();
        produto.setId(id);
        produto.setNome(nome);
        produto.setPrecoVenda(new BigDecimal(preco));
        produto.adicionarEstoque(saldo);
        return produto;
    }

    private static ItemVendaRequest item(Long produtoId, int quantidade) {
        return new ItemVendaRequest(produtoId, quantidade);
    }

    private static VendaRequest pedido(ItemVendaRequest... itens) {
        return new VendaRequest(List.of(itens));
    }
}
