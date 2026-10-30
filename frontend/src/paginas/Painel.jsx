import { useCallback, useState } from 'react'
import Aviso from '../componentes/Aviso.jsx'
import GraficoFaturamento from '../componentes/painel/GraficoFaturamento.jsx'
import GraficoRosca from '../componentes/painel/GraficoRosca.jsx'
import { IconeDeposito, IconeDinheiro, IconeGiro, IconeRuptura } from '../componentes/painel/Icones.jsx'
import Ilustracao from '../componentes/painel/Ilustracao.jsx'
import TabelaDetalhamento from '../componentes/painel/TabelaDetalhamento.jsx'
import { api } from '../servicos/api.js'
import {
  formatarDecimal,
  formatarMoeda,
  formatarMoedaCompacta,
  formatarNumero,
  formatarPercentual,
} from '../servicos/formato.js'
import { useDados } from '../servicos/useDados.js'
import './painel.css'

const CORES = ['#2f80ed', '#f23d5c', '#7a4dff', '#f2b705', '#22b573', '#14b8c4', '#ff8a3d', '#9aa0aa']
const CLASSES = ['A', 'B', 'C']

export default function Painel() {
  const [ano, setAno] = useState(null)
  const [categoriaId, setCategoriaId] = useState(0)
  const [classes, setClasses] = useState([])

  const carregar = useCallback(() => {
    const parametros = new URLSearchParams({ categoriaId: String(categoriaId) })
    if (ano) parametros.set('ano', String(ano))
    return api(`/dashboard?${parametros}`)
  }, [ano, categoriaId])
  const { dados, erro, carregando } = useDados(carregar)

  if (!dados) {
    return (
      <>
        <Aviso>{erro}</Aviso>
        {carregando && <p className="vazio">Carregando…</p>}
      </>
    )
  }

  const indicadores = dados.indicadores
  const fatias = dados.estoquePorCategoria.map((c, i) => ({
    chave: c.categoriaId,
    rotulo: c.categoria,
    valor: Number(c.valor),
    cor: CORES[i % CORES.length],
  }))
  const categoriaAtual = dados.estoquePorCategoria.find((c) => c.categoriaId === categoriaId)
  const maiorQuantidade = Math.max(1, ...dados.maisVendidos.map((m) => m.quantidade))
  const hoje = new Date()
  const ultimoMes = dados.ano === hoje.getFullYear() ? hoje.getMonth() + 1 : 12

  const alternarClasse = (classe) =>
    setClasses((atual) => (atual.includes(classe) ? atual.filter((c) => c !== classe) : [...atual, classe]))

  return (
    <>
      <Aviso>{erro}</Aviso>
      <div className="dash" aria-busy={carregando}>
        <section className="cartao dash__titulo">
          <Ilustracao />
          <div>
            <p className="dash__marca">Dashboard</p>
            <h1>Controle de estoque</h1>
            <p className="dash__contexto">
              {dados.ano}
              {categoriaAtual ? `, ${categoriaAtual.categoria}` : ', todas as categorias'}
            </p>
          </div>
        </section>

        <div className="dash__kpis">
          <Indicador cor="azul" icone={<IconeDinheiro />} rotulo="Faturamento"
            valor={formatarMoedaCompacta(indicadores.faturamento)}
            titulo={formatarMoeda(indicadores.faturamento)}
            detalhe={
              <Variacao
                valor={indicadores.variacaoPercentual}
                anoAnterior={dados.ano - 1}
                parcial={indicadores.periodoParcial}
              />
            } />
          <Indicador cor="verde" icone={<IconeDeposito />} rotulo="Valor em estoque"
            valor={formatarMoedaCompacta(indicadores.valorEmEstoque)}
            titulo={formatarMoeda(indicadores.valorEmEstoque)}
            detalhe="a preço de custo" />
          <Indicador cor="vermelho" icone={<IconeRuptura />} rotulo="Ruptura de estoque"
            valor={formatarPercentual(indicadores.rupturaPercentual, 2)}
            detalhe={`${indicadores.produtosZerados} de ${indicadores.produtosAtivos} produtos sem saldo`} />
          <Indicador cor="roxo" icone={<IconeGiro />} rotulo="Giro de estoque"
            valor={`${formatarDecimal(indicadores.giroEstoque, 2)}x`}
            detalhe={`no ano, ${formatarNumero(indicadores.pedidos)} pedidos`} />
        </div>

        <section className="cartao dash__tempo" aria-labelledby="titulo-tempo">
          <div className="cartao__topo">
            <h2 id="titulo-tempo">Faturamento ao longo do tempo</h2>
            <ul className="legenda">
              <li><span className="legenda__ponto legenda__ponto--barra" />Faturamento</li>
              <li><span className="legenda__ponto legenda__ponto--linha" />Pedidos</li>
            </ul>
          </div>
          <GraficoFaturamento meses={dados.faturamentoMensal} ultimoMes={ultimoMes} />
        </section>

        <div className="dash__lateral">
          <section className="cartao dash__filtros" aria-label="Filtros">
            <label className="campo">
              Ano
              <select value={dados.ano} onChange={(e) => setAno(Number(e.target.value))}>
                {dados.anosDisponiveis.map((a) => (
                  <option key={a} value={a}>{a}</option>
                ))}
              </select>
            </label>
            <label className="campo">
              Categoria
              <select value={categoriaId} onChange={(e) => setCategoriaId(Number(e.target.value))}>
                <option value={0}>Todas</option>
                {dados.estoquePorCategoria.map((c) => (
                  <option key={c.categoriaId} value={c.categoriaId}>{c.categoria}</option>
                ))}
              </select>
            </label>
            <div className="campo" role="group" aria-labelledby="rotulo-abc">
              <span id="rotulo-abc">Classe ABC</span>
              <div className="filtro-abc">
                {CLASSES.map((classe) => (
                  <button
                    key={classe}
                    type="button"
                    aria-pressed={classes.includes(classe)}
                    onClick={() => alternarClasse(classe)}
                  >
                    {classe}
                  </button>
                ))}
              </div>
            </div>
          </section>

          <section className="cartao dash__tabela" aria-labelledby="titulo-detalhamento">
            <h2 id="titulo-detalhamento">Detalhamento</h2>
            <TabelaDetalhamento grupos={dados.detalhamento} classes={classes} />
          </section>
        </div>

        <section className="cartao dash__rosca" aria-labelledby="titulo-rosca">
          <h2 id="titulo-rosca">Valor em estoque por categoria</h2>
          <GraficoRosca itens={fatias} />
        </section>

        <section className="cartao dash__ranking" aria-labelledby="titulo-ranking">
          <h2 id="titulo-ranking">Mais vendidos em {dados.ano}</h2>
          {dados.maisVendidos.length === 0 ? (
            <p className="vazio">Nenhuma venda concluída neste período.</p>
          ) : (
            <ol className="barras">
              {dados.maisVendidos.map((item) => (
                <li key={item.produtoId}>
                  <div className="barras__topo">
                    <span className="barras__nome">{item.nome}</span>
                    <span className="barras__qtd">{formatarNumero(item.quantidade)} un.</span>
                    <span className="barras__total">{formatarMoedaCompacta(item.total)}</span>
                  </div>
                  <span className="barras__trilho">
                    <span style={{ width: `${(item.quantidade / maiorQuantidade) * 100}%` }} />
                  </span>
                </li>
              ))}
            </ol>
          )}
        </section>
      </div>
    </>
  )
}

function Indicador({ cor, icone, rotulo, valor, titulo, detalhe }) {
  return (
    <section className="cartao kpi" aria-label={rotulo}>
      <span className={`kpi__icone kpi__icone--${cor}`}>{icone}</span>
      <div className="kpi__texto">
        <h2 className="kpi__rotulo">{rotulo}</h2>
        <p className="kpi__valor" title={titulo}>{valor}</p>
        <p className="kpi__detalhe">{detalhe}</p>
      </div>
    </section>
  )
}

function Variacao({ valor, anoAnterior, parcial }) {
  const referencia = parcial ? `mesmo período de ${anoAnterior}` : String(anoAnterior)
  if (valor === null || valor === undefined) return `sem vendas em ${referencia}`
  const numero = Number(valor)
  const subiu = numero >= 0
  return (
    <>
      <span className={subiu ? 'variacao variacao--alta' : 'variacao variacao--queda'}>
        {subiu ? '▲' : '▼'} {formatarPercentual(Math.abs(numero))}
      </span>{' '}
      vs. {referencia}
    </>
  )
}
