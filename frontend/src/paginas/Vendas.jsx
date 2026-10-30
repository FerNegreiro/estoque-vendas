import { useCallback, useState } from 'react'
import Aviso from '../componentes/Aviso.jsx'
import Modal from '../componentes/Modal.jsx'
import Paginacao from '../componentes/Paginacao.jsx'
import { useAuth } from '../contexto/AuthContext.jsx'
import { api } from '../servicos/api.js'
import { adicionarItem, removerItem, totalDoCarrinho } from '../servicos/carrinho.js'
import { formatarDataHora, formatarMoeda } from '../servicos/formato.js'
import { useDados } from '../servicos/useDados.js'

export default function Vendas() {
  const carregarProdutos = useCallback(() => api('/produtos?size=100&sort=nome'), [])
  const produtos = useDados(carregarProdutos)
  const [pagina, setPagina] = useState(0)
  const carregarVendas = useCallback(() => api(`/vendas?page=${pagina}&size=10`), [pagina])
  const vendas = useDados(carregarVendas)

  const [itens, setItens] = useState([])
  const [produtoId, setProdutoId] = useState('')
  const [quantidade, setQuantidade] = useState('1')
  const [erro, setErro] = useState('')
  const [sucesso, setSucesso] = useState('')
  const [finalizando, setFinalizando] = useState(false)
  const [vendaAberta, setVendaAberta] = useState(null)

  const disponiveis = (produtos.dados?.conteudo ?? []).filter((p) => p.quantidadeEstoque > 0)

  function adicionar(evento) {
    evento.preventDefault()
    setErro('')
    setSucesso('')
    const produto = disponiveis.find((p) => p.id === Number(produtoId))
    if (!produto) {
      setErro('Escolha um produto para adicionar.')
      return
    }
    try {
      setItens(adicionarItem(itens, produto, Number(quantidade)))
      setQuantidade('1')
    } catch (falha) {
      setErro(falha.message)
    }
  }

  async function finalizar() {
    setErro('')
    setFinalizando(true)
    try {
      const corpo = { itens: itens.map((item) => ({ produtoId: item.produtoId, quantidade: item.quantidade })) }
      const venda = await api('/vendas', { method: 'POST', body: corpo })
      setItens([])
      setProdutoId('')
      setSucesso(`Venda nº ${venda.id} registrada: ${formatarMoeda(venda.valorTotal)}.`)
      produtos.recarregar()
      vendas.recarregar()
    } catch (falha) {
      setErro(falha.message)
    } finally {
      setFinalizando(false)
    }
  }

  const aoAlterarVenda = () => {
    produtos.recarregar()
    vendas.recarregar()
  }

  return (
    <>
      <div className="cabecalho">
        <h1>Vendas</h1>
      </div>

      <div className="balcao">
        <section className="bloco" aria-labelledby="titulo-nova-venda">
          <h2 id="titulo-nova-venda">Nova venda</h2>
          <form className="formulario formulario--linha" onSubmit={adicionar}>
            <label className="campo campo--largo">
              Produto
              <select value={produtoId} onChange={(e) => setProdutoId(e.target.value)}>
                <option value="">Escolha um produto</option>
                {disponiveis.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.nome} ({p.sku}), {formatarMoeda(p.precoVenda)}, {p.quantidadeEstoque} em estoque
                  </option>
                ))}
              </select>
            </label>
            <label className="campo campo--curto">
              Quantidade
              <input
                type="number"
                min="1"
                step="1"
                inputMode="numeric"
                required
                value={quantidade}
                onChange={(e) => setQuantidade(e.target.value)}
              />
            </label>
            <button className="botao">Adicionar</button>
          </form>
          <Aviso>{erro || produtos.erro}</Aviso>
          <Aviso tipo="sucesso">{sucesso}</Aviso>
        </section>

        <Cupom
          itens={itens}
          aoRemover={(id) => setItens(removerItem(itens, id))}
          aoFinalizar={finalizar}
          finalizando={finalizando}
        />
      </div>

      <section className="bloco" aria-labelledby="titulo-historico">
        <h2 id="titulo-historico">Vendas registradas</h2>
        <Aviso>{vendas.erro}</Aviso>
        <div className="tabela-rolagem">
          <table className="tabela">
            <thead>
              <tr>
                <th scope="col">Nº</th>
                <th scope="col">Data</th>
                <th scope="col">Vendedor</th>
                <th scope="col">Situação</th>
                <th scope="col" className="num">Total</th>
                <th scope="col"><span className="sr-only">Detalhes</span></th>
              </tr>
            </thead>
            <tbody>
              {vendas.dados?.conteudo.map((venda) => (
                <tr key={venda.id}>
                  <td className="num-esq">{venda.id}</td>
                  <td>{formatarDataHora(venda.data)}</td>
                  <td>{venda.vendedor}</td>
                  <td><Situacao status={venda.status} /></td>
                  <td className="num">{formatarMoeda(venda.valorTotal)}</td>
                  <td className="acoes">
                    <button type="button" className="botao botao--discreto" onClick={() => setVendaAberta(venda.id)}>
                      Ver itens
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        {vendas.dados?.conteudo.length === 0 && <p className="vazio">Nenhuma venda registrada ainda.</p>}
        <Paginacao pagina={vendas.dados} aoMudar={setPagina} />
      </section>

      {vendaAberta && (
        <DetalheVenda id={vendaAberta} aoFechar={() => setVendaAberta(null)} aoAlterar={aoAlterarVenda} />
      )}
    </>
  )
}

function Situacao({ status }) {
  return status === 'CANCELADA'
    ? <span className="situacao situacao--cancelada">Cancelada</span>
    : <span className="situacao situacao--concluida">Concluída</span>
}

function Cupom({ itens, aoRemover, aoFinalizar, finalizando }) {
  return (
    <aside className="cupom" aria-label="Itens da venda">
      <div className="cupom__sombra">
        <div className="cupom__papel">
          <p className="cupom__titulo">Balcão</p>
          <p className="cupom__subtitulo">Venda em andamento</p>
          <hr className="cupom__corte" />
          {itens.length === 0 ? (
            <p className="cupom__vazio">Adicione produtos ao lado para montar a venda.</p>
          ) : (
            <ul className="cupom__itens">
              {itens.map((item) => (
                <li key={item.produtoId} className="cupom__item">
                  <span className="cupom__nome">{item.nome}</span>
                  <span className="cupom__conta">
                    {item.quantidade} x {formatarMoeda(item.precoUnitario)}
                  </span>
                  <span className="cupom__valor">{formatarMoeda(totalDoCarrinho([item]))}</span>
                  <button
                    type="button"
                    className="cupom__remover"
                    onClick={() => aoRemover(item.produtoId)}
                    aria-label={`Remover ${item.nome}`}
                  >
                    remover
                  </button>
                </li>
              ))}
            </ul>
          )}
          <hr className="cupom__corte" />
          <p className="cupom__total">
            <span>Total</span>
            <strong>{formatarMoeda(totalDoCarrinho(itens))}</strong>
          </p>
        </div>
      </div>
      <button
        type="button"
        className="botao botao--primario botao--cheio"
        disabled={itens.length === 0 || finalizando}
        onClick={aoFinalizar}
      >
        {finalizando ? 'Registrando…' : 'Finalizar venda'}
      </button>
    </aside>
  )
}

function DetalheVenda({ id, aoFechar, aoAlterar }) {
  const { ehAdmin } = useAuth()
  const carregar = useCallback(() => api(`/vendas/${id}`), [id])
  const { dados: venda, erro, recarregar } = useDados(carregar)
  const [erroAcao, setErroAcao] = useState('')
  const [cancelando, setCancelando] = useState(false)

  async function cancelar() {
    if (!window.confirm('Cancelar esta venda? Os itens voltam para o estoque.')) return
    setErroAcao('')
    setCancelando(true)
    try {
      await api(`/vendas/${id}/cancelamento`, { method: 'POST' })
      recarregar()
      aoAlterar()
    } catch (falha) {
      setErroAcao(falha.message)
    } finally {
      setCancelando(false)
    }
  }

  return (
    <Modal titulo={`Venda nº ${id}`} aoFechar={aoFechar} largura="estreita">
      <Aviso>{erro || erroAcao}</Aviso>
      {venda && (
        <>
          <p className="detalhe-venda__meta">
            {formatarDataHora(venda.data)}, por {venda.vendedor}. <Situacao status={venda.status} />
          </p>
          <ul className="lista-simples">
            {venda.itens.map((item) => (
              <li key={item.produtoId}>
                <span>{item.quantidade} x {item.produtoNome}</span>
                <span className="num">{formatarMoeda(item.subtotal)}</span>
              </li>
            ))}
            <li className="lista-simples__total">
              <span>Total</span>
              <span className="num">{formatarMoeda(venda.valorTotal)}</span>
            </li>
          </ul>
          {ehAdmin && venda.status === 'CONCLUIDA' && (
            <button type="button" className="botao botao--perigo-cheio" onClick={cancelar} disabled={cancelando}>
              {cancelando ? 'Cancelando…' : 'Cancelar venda'}
            </button>
          )}
        </>
      )}
    </Modal>
  )
}
