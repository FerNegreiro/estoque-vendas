package br.com.estoque.config;

import br.com.estoque.domain.Categoria;
import br.com.estoque.domain.MovimentacaoEstoque;
import br.com.estoque.domain.Perfil;
import br.com.estoque.domain.Produto;
import br.com.estoque.domain.TipoMovimentacao;
import br.com.estoque.domain.Usuario;
import br.com.estoque.domain.Venda;
import br.com.estoque.repository.CategoriaRepository;
import br.com.estoque.repository.MovimentacaoEstoqueRepository;
import br.com.estoque.repository.ProdutoRepository;
import br.com.estoque.repository.UsuarioRepository;
import br.com.estoque.repository.VendaRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DadosIniciais implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DadosIniciais.class);
    private static final long SEMENTE = 2026L;
    private static final double[] SAZONALIDADE = {0.9, 0.95, 1.1, 1.05, 1.15, 0.75, 0.55, 0.85, 0.95, 1.0, 1.1, 1.35};

    private record Demo(String sku, String nome, String categoria, String custo, String venda,
                        int minimo, int saldoFinal, int popularidade) {
    }

    private record ItemPlanejado(int produto, int quantidade) {
    }

    private record VendaPlanejada(OffsetDateTime data, List<ItemPlanejado> itens) {
    }

    private static final List<Demo> PRODUTOS_DEMO = List.of(
            new Demo("ALI-001", "Arroz tipo 1 5 kg", "Alimentos", "18.00", "27.90", 10, 35, 9),
            new Demo("ALI-002", "Feijão carioca 1 kg", "Alimentos", "5.40", "8.99", 10, 8, 8),
            new Demo("ALI-003", "Café torrado 500 g", "Alimentos", "11.80", "19.90", 12, 30, 7),
            new Demo("ALI-004", "Óleo de soja 900 ml", "Alimentos", "5.10", "7.99", 12, 26, 6),
            new Demo("ALI-005", "Macarrão espaguete 500 g", "Alimentos", "2.90", "4.79", 15, 44, 6),
            new Demo("BEB-001", "Água mineral 500 ml", "Bebidas", "0.90", "2.50", 24, 120, 10),
            new Demo("BEB-002", "Refrigerante lata 350 ml", "Bebidas", "2.10", "4.50", 24, 60, 9),
            new Demo("BEB-003", "Suco integral 1 l", "Bebidas", "6.20", "10.90", 8, 18, 4),
            new Demo("HIG-001", "Sabonete 90 g", "Higiene", "1.20", "2.49", 20, 0, 6),
            new Demo("HIG-002", "Creme dental 90 g", "Higiene", "2.80", "5.49", 15, 32, 5),
            new Demo("HIG-003", "Papel higiênico 12 rolos", "Higiene", "14.50", "22.90", 8, 14, 5),
            new Demo("LIM-001", "Detergente 500 ml", "Limpeza", "1.60", "2.99", 12, 40, 6),
            new Demo("LIM-002", "Sabão em pó 1 kg", "Limpeza", "9.80", "15.90", 8, 21, 4),
            new Demo("LIM-003", "Água sanitária 2 l", "Limpeza", "3.40", "6.49", 10, 6, 3),
            new Demo("PAP-001", "Caderno 96 folhas", "Papelaria", "6.50", "12.90", 5, 22, 3),
            new Demo("PAP-002", "Caneta esferográfica azul", "Papelaria", "0.85", "2.20", 30, 85, 4));

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;
    private final VendaRepository vendaRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminSenha;
    private final boolean dadosDemo;
    private final ZoneId fusoHorario;

    public DadosIniciais(UsuarioRepository usuarioRepository, CategoriaRepository categoriaRepository,
                         ProdutoRepository produtoRepository, VendaRepository vendaRepository,
                         MovimentacaoEstoqueRepository movimentacaoRepository, PasswordEncoder passwordEncoder,
                         @Value("${app.admin.email}") String adminEmail,
                         @Value("${app.admin.senha}") String adminSenha,
                         @Value("${app.dados-demo}") boolean dadosDemo,
                         @Value("${app.fuso-horario}") String fusoHorario) {
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.produtoRepository = produtoRepository;
        this.vendaRepository = vendaRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminSenha = adminSenha;
        this.dadosDemo = dadosDemo;
        this.fusoHorario = ZoneId.of(fusoHorario);
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Usuario admin = garantirAdministrador();
        if (dadosDemo && admin != null && produtoRepository.count() == 0) {
            criarDemonstracao(admin);
        }
    }

    private Usuario garantirAdministrador() {
        if (usuarioRepository.count() > 0) {
            return usuarioRepository.findByEmailIgnoreCase(adminEmail).orElse(null);
        }
        if (adminSenha == null || adminSenha.length() < 8) {
            log.warn("Nenhum usuário cadastrado. Defina ADMIN_SENHA (mínimo 8 caracteres) para criar o administrador.");
            return null;
        }
        Usuario admin = new Usuario("Administrador", adminEmail.toLowerCase(),
                passwordEncoder.encode(adminSenha), Perfil.ADMIN);
        log.info("Administrador inicial criado: {}", admin.getEmail());
        return usuarioRepository.save(admin);
    }

    private void criarDemonstracao(Usuario admin) {
        ZonedDateTime agora = ZonedDateTime.now(fusoHorario);
        ZonedDateTime inicio = agora.minusYears(1).withDayOfYear(1).truncatedTo(ChronoUnit.DAYS);
        Random aleatorio = new Random(SEMENTE);

        List<VendaPlanejada> plano = planejarVendas(inicio, agora, aleatorio);
        int[] vendidos = new int[PRODUTOS_DEMO.size()];
        plano.forEach(venda -> venda.itens().forEach(item -> vendidos[item.produto()] += item.quantidade()));

        List<Produto> produtos = cadastrarProdutos();
        OffsetDateTime dataDaCarga = inicio.minusDays(1).withHour(8).toOffsetDateTime();
        for (int i = 0; i < produtos.size(); i++) {
            int entrada = PRODUTOS_DEMO.get(i).saldoFinal() + vendidos[i];
            if (entrada > 0) {
                produtos.get(i).adicionarEstoque(entrada);
                movimentacaoRepository.save(new MovimentacaoEstoque(produtos.get(i), TipoMovimentacao.ENTRADA,
                        entrada, "Carga de demonstração", admin, null, dataDaCarga));
            }
        }

        for (VendaPlanejada planejada : plano) {
            Venda venda = vendaRepository.save(new Venda(admin, planejada.data()));
            for (ItemPlanejado item : planejada.itens()) {
                Produto produto = produtos.get(item.produto());
                produto.baixarEstoque(item.quantidade());
                venda.adicionarItem(produto, item.quantidade());
                movimentacaoRepository.save(new MovimentacaoEstoque(produto, TipoMovimentacao.SAIDA,
                        item.quantidade(), "Venda #" + venda.getId(), admin, venda, planejada.data()));
            }
        }
        log.info("Demonstração criada: {} produtos e {} vendas", produtos.size(), plano.size());
    }

    private List<Produto> cadastrarProdutos() {
        Map<String, Categoria> categorias = new LinkedHashMap<>();
        List<Produto> produtos = new ArrayList<>();
        for (Demo demo : PRODUTOS_DEMO) {
            Categoria categoria = categorias.computeIfAbsent(demo.categoria(), nome ->
                    categoriaRepository.findByNomeIgnoreCase(nome)
                            .orElseGet(() -> categoriaRepository.save(new Categoria(nome))));
            Produto produto = new Produto();
            produto.setSku(demo.sku());
            produto.setNome(demo.nome());
            produto.setCategoria(categoria);
            produto.setPrecoCusto(new BigDecimal(demo.custo()));
            produto.setPrecoVenda(new BigDecimal(demo.venda()));
            produto.setEstoqueMinimo(demo.minimo());
            produtos.add(produtoRepository.save(produto));
        }
        return produtos;
    }

    private List<VendaPlanejada> planejarVendas(ZonedDateTime inicio, ZonedDateTime agora, Random aleatorio) {
        int pesoTotal = PRODUTOS_DEMO.stream().mapToInt(Demo::popularidade).sum();
        List<VendaPlanejada> plano = new ArrayList<>();

        for (ZonedDateTime mes = inicio; mes.isBefore(agora); mes = mes.plusMonths(1)) {
            double crescimento = mes.getYear() == agora.getYear() ? 1.0 : 0.78;
            int quantidadeDeVendas = (int) Math.round(
                    (26 + aleatorio.nextInt(8)) * SAZONALIDADE[mes.getMonthValue() - 1] * crescimento);
            int diasNoMes = mes.toLocalDate().lengthOfMonth();

            for (int v = 0; v < quantidadeDeVendas; v++) {
                ZonedDateTime data = mes.plusDays(aleatorio.nextInt(diasNoMes))
                        .withHour(8 + aleatorio.nextInt(12))
                        .withMinute(aleatorio.nextInt(60));
                if (data.isAfter(agora)) {
                    continue;
                }
                plano.add(new VendaPlanejada(data.toOffsetDateTime(), sortearItens(aleatorio, pesoTotal)));
            }
        }
        plano.sort(Comparator.comparing(VendaPlanejada::data));
        return plano;
    }

    private List<ItemPlanejado> sortearItens(Random aleatorio, int pesoTotal) {
        int quantidadeDeItens = 1 + aleatorio.nextInt(3);
        Map<Integer, Integer> itens = new LinkedHashMap<>();
        while (itens.size() < quantidadeDeItens) {
            int sorteio = aleatorio.nextInt(pesoTotal);
            int indice = 0;
            while (sorteio >= PRODUTOS_DEMO.get(indice).popularidade()) {
                sorteio -= PRODUTOS_DEMO.get(indice).popularidade();
                indice++;
            }
            itens.putIfAbsent(indice, 1 + aleatorio.nextInt(4));
        }
        return itens.entrySet().stream()
                .map(entrada -> new ItemPlanejado(entrada.getKey(), entrada.getValue()))
                .toList();
    }
}
