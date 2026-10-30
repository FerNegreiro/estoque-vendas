package br.com.estoque.domain;

import br.com.estoque.exception.NegocioException;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vendas")
@Getter
@NoArgsConstructor
public class Venda {

    @Id
    @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "data_venda", nullable = false)
    private OffsetDateTime dataVenda = OffsetDateTime.now();

    @Column(name = "valor_total", nullable = false, precision = 14, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusVenda status = StatusVenda.CONCLUIDA;

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemVenda> itens = new ArrayList<>();

    public Venda(Usuario usuario) {
        this.usuario = usuario;
    }

    public Venda(Usuario usuario, OffsetDateTime dataVenda) {
        this.usuario = usuario;
        this.dataVenda = dataVenda;
    }

    public void adicionarItem(Produto produto, int quantidade) {
        ItemVenda item = new ItemVenda(this, produto, quantidade);
        itens.add(item);
        valorTotal = valorTotal.add(item.getSubtotal());
    }

    public void cancelar() {
        if (status == StatusVenda.CANCELADA) {
            throw new NegocioException("Esta venda já foi cancelada");
        }
        status = StatusVenda.CANCELADA;
    }
}
