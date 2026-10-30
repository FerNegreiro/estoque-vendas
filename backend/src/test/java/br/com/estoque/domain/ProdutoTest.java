package br.com.estoque.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.estoque.exception.NegocioException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ProdutoTest {

    private Produto produtoComSaldo(int saldo) {
        Produto produto = new Produto();
        produto.setNome("Café 500 g");
        produto.setPrecoVenda(new BigDecimal("19.90"));
        produto.setEstoqueMinimo(3);
        if (saldo > 0) {
            produto.adicionarEstoque(saldo);
        }
        return produto;
    }

    @Test
    void deveBaixarEstoqueQuandoHaSaldo() {
        Produto produto = produtoComSaldo(10);

        produto.baixarEstoque(4);

        assertThat(produto.getQuantidadeEstoque()).isEqualTo(6);
    }

    @Test
    void deveRecusarBaixaMaiorQueOSaldoSemAlterarOEstoque() {
        Produto produto = produtoComSaldo(5);

        assertThatThrownBy(() -> produto.baixarEstoque(6))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("Estoque insuficiente");
        assertThat(produto.getQuantidadeEstoque()).isEqualTo(5);
    }

    @Test
    void deveRecusarQuantidadeZeroOuNegativa() {
        Produto produto = produtoComSaldo(5);

        assertThatThrownBy(() -> produto.adicionarEstoque(0)).isInstanceOf(NegocioException.class);
        assertThatThrownBy(() -> produto.baixarEstoque(-1)).isInstanceOf(NegocioException.class);
    }

    @Test
    void deveSinalizarEstoqueBaixoQuandoSaldoChegaAoMinimo() {
        Produto produto = produtoComSaldo(4);
        assertThat(produto.isEstoqueBaixo()).isFalse();

        produto.baixarEstoque(1);

        assertThat(produto.isEstoqueBaixo()).isTrue();
    }

    @Test
    void itemDeVendaDeveCongelarOPrecoECalcularSubtotal() {
        Produto produto = produtoComSaldo(10);
        Venda venda = new Venda(new Usuario());

        venda.adicionarItem(produto, 3);
        produto.setPrecoVenda(new BigDecimal("99.00"));

        assertThat(venda.getItens().get(0).getPrecoUnitario()).isEqualByComparingTo("19.90");
        assertThat(venda.getValorTotal()).isEqualByComparingTo("59.70");
    }
}
