export function adicionarItem(itens, produto, quantidade) {
  if (!Number.isInteger(quantidade) || quantidade < 1) {
    throw new Error('Informe uma quantidade inteira maior que zero')
  }
  const existente = itens.find((item) => item.produtoId === produto.id)
  const novaQuantidade = (existente?.quantidade ?? 0) + quantidade
  if (novaQuantidade > produto.quantidadeEstoque) {
    throw new Error(`Só há ${produto.quantidadeEstoque} unidade(s) de ${produto.nome} em estoque`)
  }
  if (existente) {
    return itens.map((item) =>
      item.produtoId === produto.id ? { ...item, quantidade: novaQuantidade } : item,
    )
  }
  return [
    ...itens,
    {
      produtoId: produto.id,
      sku: produto.sku,
      nome: produto.nome,
      precoUnitario: Number(produto.precoVenda),
      quantidade,
    },
  ]
}

export const removerItem = (itens, produtoId) => itens.filter((item) => item.produtoId !== produtoId)

export function totalDoCarrinho(itens) {
  const centavos = itens.reduce(
    (soma, item) => soma + Math.round(item.precoUnitario * 100) * item.quantidade,
    0,
  )
  return centavos / 100
}
