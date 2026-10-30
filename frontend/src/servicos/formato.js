const moeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })
const numero = new Intl.NumberFormat('pt-BR')
const dataHora = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' })
const mesAno = new Intl.DateTimeFormat('pt-BR', { month: 'long', year: 'numeric' })
const compacto = new Intl.NumberFormat('pt-BR', { notation: 'compact', maximumFractionDigits: 1 })

export const formatarMoeda = (valor) => moeda.format(Number(valor ?? 0))
export const formatarNumero = (valor) => numero.format(Number(valor ?? 0))
export const formatarDataHora = (iso) => (iso ? dataHora.format(new Date(iso)) : '')
export const formatarMesAno = (data = new Date()) => mesAno.format(data)
export const formatarCompacto = (valor) => compacto.format(Number(valor ?? 0))

export function formatarMoedaCompacta(valor) {
  const numero = Number(valor ?? 0)
  return Math.abs(numero) < 1000 ? formatarMoeda(numero) : `R$ ${compacto.format(numero)}`
}

export function formatarDecimal(valor, casas = 1) {
  return new Intl.NumberFormat('pt-BR', { minimumFractionDigits: casas, maximumFractionDigits: casas })
    .format(Number(valor ?? 0))
}

export const formatarPercentual = (valor, casas = 1) => `${formatarDecimal(valor, casas)}%`

export function paraNumero(texto) {
  if (typeof texto === 'number') return texto
  const limpo = String(texto ?? '').trim().replace(/\s/g, '').replace(/^R\$/, '')
  if (!limpo) return NaN
  const normalizado = limpo.includes(',') ? limpo.replace(/\./g, '').replace(',', '.') : limpo
  return /^-?\d+(\.\d+)?$/.test(normalizado) ? Number(normalizado) : NaN
}
