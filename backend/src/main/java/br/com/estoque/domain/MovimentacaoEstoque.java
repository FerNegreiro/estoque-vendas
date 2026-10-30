package br.com.estoque.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "movimentacoes_estoque")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MovimentacaoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoMovimentacao tipo;

    @Column(nullable = false)
    private int quantidade;

    @Column(nullable = false, length = 200)
    private String motivo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venda_id")
    private Venda venda;

    @Column(name = "data_movimentacao", nullable = false, updatable = false)
    private OffsetDateTime dataMovimentacao = OffsetDateTime.now();

    public MovimentacaoEstoque(Produto produto, TipoMovimentacao tipo, int quantidade,
                               String motivo, Usuario usuario, Venda venda) {
        this(produto, tipo, quantidade, motivo, usuario, venda, OffsetDateTime.now());
    }

    public MovimentacaoEstoque(Produto produto, TipoMovimentacao tipo, int quantidade,
                               String motivo, Usuario usuario, Venda venda, OffsetDateTime dataMovimentacao) {
        this.dataMovimentacao = dataMovimentacao;
        this.produto = produto;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.motivo = motivo;
        this.usuario = usuario;
        this.venda = venda;
    }
}
