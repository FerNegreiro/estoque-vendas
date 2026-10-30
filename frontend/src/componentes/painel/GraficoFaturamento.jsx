import { useEffect, useRef, useState } from 'react'
import { formatarCompacto, formatarMoeda, formatarNumero } from '../../servicos/formato.js'

const MESES = ['jan', 'fev', 'mar', 'abr', 'mai', 'jun', 'jul', 'ago', 'set', 'out', 'nov', 'dez']
const LARGURA_MINIMA = 320
const ALTURA = 260
const MARGEM = { topo: 14, direita: 52, base: 26, esquerda: 6 }
const DIVISOES_ALVO = 5
const FOLGA = 1.05

function passoArredondado(bruto) {
  if (bruto <= 0) return 1
  const potencia = 10 ** Math.floor(Math.log10(bruto))
  const fator = bruto / potencia
  const arredondado = fator <= 1 ? 1 : fator <= 2 ? 2 : fator <= 2.5 ? 2.5 : fator <= 5 ? 5 : 10
  return arredondado * potencia
}

function useLargura(inicial) {
  const referencia = useRef(null)
  const [largura, setLargura] = useState(inicial)

  useEffect(() => {
    const elemento = referencia.current
    if (!elemento || typeof ResizeObserver === 'undefined') return undefined
    const observador = new ResizeObserver(([entrada]) =>
      setLargura(Math.max(LARGURA_MINIMA, Math.round(entrada.contentRect.width))),
    )
    observador.observe(elemento)
    return () => observador.disconnect()
  }, [])

  return [referencia, largura]
}

export default function GraficoFaturamento({ meses, ultimoMes = 12 }) {
  const [referencia, largura] = useLargura(640)

  const valores = meses.map((m) => Number(m.faturamento))
  const maior = Math.max(...valores, 0)
  const passo = passoArredondado(maior / DIVISOES_ALVO)
  const divisoes = Math.max(1, Math.ceil((maior * FOLGA) / passo))
  const topo = passo * divisoes
  const maxPedidos = Math.max(...meses.map((m) => m.pedidos), 1) * 1.15

  const areaAltura = ALTURA - MARGEM.topo - MARGEM.base
  const faixa = (largura - MARGEM.esquerda - MARGEM.direita) / meses.length
  const larguraBarra = Math.min(faixa * 0.62, 42)
  const yValor = (valor) => MARGEM.topo + areaAltura * (1 - valor / topo)
  const yPedidos = (pedidos) => MARGEM.topo + areaAltura * (1 - pedidos / maxPedidos)
  const xCentro = (indice) => MARGEM.esquerda + faixa * indice + faixa / 2

  const comLinha = meses.slice(0, ultimoMes)
  const pontos = comLinha.map((m, i) => `${xCentro(i)},${yPedidos(m.pedidos)}`).join(' ')
  const total = valores.reduce((a, b) => a + b, 0)

  return (
    <div ref={referencia} className="grafico-tempo__caixa">
      <svg
        className="grafico-tempo"
        viewBox={`0 0 ${largura} ${ALTURA}`}
        role="img"
        aria-label={`Faturamento mensal, total de ${formatarMoeda(total)}`}
      >
        {Array.from({ length: divisoes + 1 }, (_, i) => {
          const valor = passo * i
          const y = yValor(valor)
          return (
            <g key={i}>
              <line x1={MARGEM.esquerda} x2={largura - MARGEM.direita} y1={y} y2={y} className="grafico-tempo__grade" />
              {i > 0 && (
                <text x={largura - MARGEM.direita + 8} y={y + 4} className="grafico-tempo__eixo">
                  {formatarCompacto(valor)}
                </text>
              )}
            </g>
          )
        })}

        {meses.map((m, i) => {
          const y = yValor(Number(m.faturamento))
          return (
            <g key={m.mes}>
              <rect
                x={xCentro(i) - larguraBarra / 2}
                y={y}
                width={larguraBarra}
                height={Math.max(0, ALTURA - MARGEM.base - y)}
                rx="3"
                className="grafico-tempo__barra"
              >
                <title>{`${MESES[i]}: ${formatarMoeda(m.faturamento)} em ${formatarNumero(m.pedidos)} pedidos`}</title>
              </rect>
              <text x={xCentro(i)} y={ALTURA - 6} textAnchor="middle" className="grafico-tempo__mes">
                {MESES[i]}
              </text>
            </g>
          )
        })}

        <polyline points={pontos} className="grafico-tempo__linha" />
        {comLinha.map((m, i) => (
          <circle key={m.mes} cx={xCentro(i)} cy={yPedidos(m.pedidos)} r="3.5" className="grafico-tempo__ponto" />
        ))}
      </svg>
    </div>
  )
}
