# Modelo de dados

Banco relacional PostgreSQL, com o esquema versionado por migrações do Flyway
(`backend/src/main/resources/db/migration`). O Hibernate roda em modo `validate`:
ele confere se as entidades batem com o banco, mas nunca altera tabelas sozinho.

```mermaid
erDiagram
    USUARIOS ||--o{ VENDAS : registra
    USUARIOS ||--o{ MOVIMENTACOES_ESTOQUE : responde_por
    CATEGORIAS ||--o{ PRODUTOS : agrupa
    PRODUTOS ||--o{ ITENS_VENDA : aparece_em
    PRODUTOS ||--o{ MOVIMENTACOES_ESTOQUE : tem
    VENDAS ||--|{ ITENS_VENDA : contem
    VENDAS ||--o{ MOVIMENTACOES_ESTOQUE : gera

    USUARIOS {
        bigint id PK
        varchar nome
        varchar email UK
        varchar senha_hash
        varchar perfil "ADMIN ou VENDEDOR"
        boolean ativo
        timestamptz criado_em
    }
    CATEGORIAS {
        bigint id PK
        varchar nome UK
    }
    PRODUTOS {
        bigint id PK
        varchar sku UK
        varchar nome
        bigint categoria_id FK
        numeric preco_custo
        numeric preco_venda
        int quantidade_estoque "CHECK >= 0"
        int estoque_minimo
        boolean ativo
        bigint versao "concorrencia otimista"
    }
    VENDAS {
        bigint id PK
        bigint usuario_id FK
        timestamptz data_venda
        numeric valor_total
        varchar status "CONCLUIDA ou CANCELADA"
    }
    ITENS_VENDA {
        bigint id PK
        bigint venda_id FK
        bigint produto_id FK
        int quantidade "CHECK > 0"
        numeric preco_unitario "preco congelado"
        numeric subtotal
    }
    MOVIMENTACOES_ESTOQUE {
        bigint id PK
        bigint produto_id FK
        varchar tipo "ENTRADA ou SAIDA"
        int quantidade
        varchar motivo
        bigint usuario_id FK
        bigint venda_id FK "opcional"
        timestamptz data_movimentacao
    }
```

## Decisões de modelagem

**Normalização (3FN).** Categoria é uma tabela própria, e cada venda tem seus itens
em `itens_venda`, em vez de repetir dados do produto dentro da venda.

**Preço congelado no item.** `itens_venda.preco_unitario` guarda o preço do momento
da venda. Se o preço do produto mudar depois, o histórico continua correto. É uma
desnormalização proposital, porque o dado tem significado histórico.

**Trilha de auditoria.** O saldo (`produtos.quantidade_estoque`) só muda junto com
um registro em `movimentacoes_estoque`, que diz quem mexeu, quando e por quê.
A soma das entradas menos as saídas de um produto sempre bate com o saldo.

**Exclusão lógica.** Produtos e usuários são desativados (`ativo = false`), nunca
apagados, para não quebrar as chaves estrangeiras de vendas antigas.

**Integridade no próprio banco.** `CHECK` impede saldo negativo, preço negativo e
quantidade zero; índices únicos em `lower(email)`, `lower(sku)` e `lower(nome)`
impedem duplicados que diferem só em maiúsculas e minúsculas.

**Valores monetários.** `NUMERIC(12,2)` no banco e `BigDecimal` no Java: nada de
`double` para dinheiro.

**Índices.** Além das chaves, há índices para as consultas mais frequentes:
vendas por data (painel do mês), itens por venda e por produto, e movimentações
por produto ordenadas por data.

**Concorrência.** Duas proteções trabalham juntas:
- a venda trava as linhas dos produtos com `SELECT ... FOR UPDATE`, sempre na
  mesma ordem (por id), o que evita vender o mesmo saldo duas vezes e evita deadlock;
- a coluna `versao` (`@Version`) faz uma edição de cadastro falhar com erro 409 se
  o produto tiver sido alterado no meio do caminho, em vez de sobrescrever o saldo.
