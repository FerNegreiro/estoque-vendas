import { describe, expect, it } from 'vitest'
import { adicionarItem, removerItem, totalDoCarrinho } from './carrinho.js'

const arroz = { id: 1, sku: 'ALI-001', nome: 'Arroz 5 kg', precoVenda: 27.9, quantidadeEstoque: 10 }
const feijao = { id: 2, sku: 'ALI-002', nome: 'Feijão 1 kg', precoVenda: 8.99, quantidadeEstoque: 3 }

describe('carrinho', () => {
  it('soma a quantidade quando o produto já está no carrinho', () => {
    let itens = adicionarItem([], arroz, 2)
    itens = adicionarItem(itens, arroz, 3)
    expect(itens).toHaveLength(1)
    expect(itens[0].quantidade).toBe(5)
  })

  it('não deixa passar do estoque disponível', () => {
    const itens = adicionarItem([], feijao, 2)
    expect(() => adicionarItem(itens, feijao, 2)).toThrow(/Só há 3/)
  })

  it('recusa quantidade zero ou fracionada', () => {
    expect(() => adicionarItem([], arroz, 0)).toThrow()
    expect(() => adicionarItem([], arroz, 1.5)).toThrow()
  })

  it('calcula o total sem erro de arredondamento', () => {
    const itens = adicionarItem(adicionarItem([], arroz, 2), feijao, 3)
    expect(totalDoCarrinho(itens)).toBe(82.77)
  })

  it('remove um produto', () => {
    const itens = adicionarItem(adicionarItem([], arroz, 1), feijao, 1)
    expect(removerItem(itens, 1).map((i) => i.produtoId)).toEqual([2])
  })
})
