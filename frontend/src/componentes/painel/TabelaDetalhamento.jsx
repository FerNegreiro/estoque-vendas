import { useState } from 'react'
import { formatarMoeda, formatarNumero } from '../../servicos/formato.js'
import SeloAbc from './SeloAbc.jsx'

export default function TabelaDetalhamento({ grupos, classes }) {
  const [recolhidas, setRecolhidas] = useState(() => new Set())

  const filtrados = grupos
    .map((grupo) => ({
      ...grupo,
      produtos: classes.length === 0 ? grupo.produtos : grupo.produtos.filter((p) => classes.includes(p.classeAbc)),
    }))
    .filter((grupo) => grupo.produtos.length > 0)

  const maiorValor = Math.max(1, ...filtrados.flatMap((g) => g.produtos.map((p) => Number(p.valorEstoque))))

  function alternar(categoriaId) {
    setRecolhidas((atual) => {
      const proximo = new Set(atual)
      if (proximo.has(categoriaId)) {
        proximo.delete(categoriaId)
      } else {
        proximo.add(categoriaId)
      }
      return proximo
    })
  }

  if (filtrados.length === 0) {
    return <p className="vazio">Nenhum produto nas classes selecionadas.</p>
  }

  return (
    <div className="detalhe">
      <table className="detalhe__tabela">
        <thead>
          <tr>
            <th scope="col">Categoria e produto</th>
            <th scope="col" className="num">Qtd.</th>
            <th scope="col" className="num">Valor estoque</th>
            <th scope="col" className="detalhe__abc">ABC</th>
          </tr>
        </thead>
        {filtrados.map((grupo) => {
          const aberta = !recolhidas.has(grupo.categoriaId)
          return (
            <tbody key={grupo.categoriaId}>
              <tr className="detalhe__grupo">
                <th scope="rowgroup">
                  <button
                    type="button"
                    className="detalhe__alternar"
                    aria-expanded={aberta}
                    onClick={() => alternar(grupo.categoriaId)}
                  >
                    <span className="detalhe__sinal" aria-hidden="true">{aberta ? '−' : '+'}</span>
                    {grupo.categoria}
                  </button>
                </th>
                <td className="num">{formatarNumero(grupo.quantidade)}</td>
                <td className="num">{formatarMoeda(grupo.valorEstoque)}</td>
                <td />
              </tr>
              {aberta &&
                grupo.produtos.map((produto) => (
                  <tr key={produto.produtoId}>
                    <td className="detalhe__produto">{produto.nome}</td>
                    <td className="num">
                      <span className={produto.estoqueBaixo ? 'saldo saldo--baixo' : 'saldo'}>
                        {formatarNumero(produto.quantidade)}
                      </span>
                    </td>
                    <td className="num">
                      <span className="detalhe__valor">
                        <span
                          className="detalhe__barra"
                          style={{ width: `${(Number(produto.valorEstoque) / maiorValor) * 100}%` }}
                        />
                        <span className="detalhe__numero">{formatarMoeda(produto.valorEstoque)}</span>
                      </span>
                    </td>
                    <td className="detalhe__abc">
                      <SeloAbc classe={produto.classeAbc} />
                    </td>
                  </tr>
                ))}
            </tbody>
          )
        })}
      </table>
    </div>
  )
}
