import { formatarMoedaCompacta, formatarPercentual } from '../../servicos/formato.js'
import { IconeCaixa } from './Icones.jsx'

const RAIO = 70
const CIRCUNFERENCIA = 2 * Math.PI * RAIO

export default function GraficoRosca({ itens }) {
  const total = itens.reduce((soma, item) => soma + item.valor, 0)
  let acumulado = 0

  return (
    <div className="rosca">
      <div className="rosca__grafico">
        <svg viewBox="0 0 200 200" role="img" aria-label="Participação de cada categoria no valor em estoque">
          <circle cx="100" cy="100" r={RAIO} className="rosca__trilho" />
          {total > 0 &&
            itens.map((item) => {
              const comprimento = (item.valor / total) * CIRCUNFERENCIA
              const segmento = (
                <circle
                  key={item.chave}
                  cx="100"
                  cy="100"
                  r={RAIO}
                  className="rosca__fatia"
                  stroke={item.cor}
                  strokeDasharray={`${comprimento} ${CIRCUNFERENCIA - comprimento}`}
                  strokeDashoffset={-acumulado}
                  transform="rotate(-90 100 100)"
                >
                  <title>{`${item.rotulo}: ${formatarPercentual((item.valor / total) * 100)}`}</title>
                </circle>
              )
              acumulado += comprimento
              return segmento
            })}
        </svg>
        <span className="rosca__centro">
          <IconeCaixa />
        </span>
      </div>
      <ul className="rosca__legenda">
        {itens.map((item) => (
          <li key={item.chave}>
            <span className="rosca__cor" style={{ background: item.cor }} />
            <span className="rosca__rotulo">{item.rotulo}</span>
            <span className="rosca__pct">{total > 0 ? formatarPercentual((item.valor / total) * 100) : '0,0%'}</span>
            <span className="rosca__valor">{formatarMoedaCompacta(item.valor)}</span>
          </li>
        ))}
      </ul>
    </div>
  )
}
