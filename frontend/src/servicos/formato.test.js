import { describe, expect, it } from 'vitest'
import { formatarMoeda, formatarMoedaCompacta, formatarPercentual, paraNumero } from './formato.js'

describe('paraNumero', () => {
  it('entende o formato brasileiro com milhar e vírgula', () => {
    expect(paraNumero('1.234,56')).toBe(1234.56)
  })

  it('entende ponto como separador decimal', () => {
    expect(paraNumero('27.90')).toBe(27.9)
  })

  it('aceita o prefixo R$', () => {
    expect(paraNumero('R$ 8,99')).toBe(8.99)
  })

  it('retorna NaN para texto inválido ou vazio', () => {
    expect(paraNumero('abc')).toBeNaN()
    expect(paraNumero('')).toBeNaN()
  })
})

describe('formatarMoeda', () => {
  it('formata em reais', () => {
    expect(formatarMoeda(1234.5)).toMatch(/R\$\s1\.234,50/)
  })
})

describe('formatarMoedaCompacta', () => {
  it('abrevia milhares e milhões no padrão brasileiro', () => {
    expect(formatarMoedaCompacta(13642.35)).toMatch(/^R\$ 13,6\smil$/)
    expect(formatarMoedaCompacta(912000000)).toMatch(/^R\$ 912\smi$/)
  })

  it('mostra o valor completo abaixo de mil', () => {
    expect(formatarMoedaCompacta(850)).toMatch(/R\$\s850,00/)
  })
})

describe('formatarPercentual', () => {
  it('usa vírgula decimal', () => {
    expect(formatarPercentual(6.99, 2)).toBe('6,99%')
  })
})
