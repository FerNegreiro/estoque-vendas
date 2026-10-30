import { useCallback, useState } from 'react'
import Aviso from '../componentes/Aviso.jsx'
import Paginacao from '../componentes/Paginacao.jsx'
import { useAuth } from '../contexto/AuthContext.jsx'
import { api } from '../servicos/api.js'
import { formatarDataHora, formatarNumero } from '../servicos/formato.js'
import { useDados } from '../servicos/useDados.js'

export default function Estoque() {
  const { ehAdmin } = useAuth()
  const [pagina, setPagina] = useState(0)
  const carregar = useCallback(() => api(`/estoque/movimentacoes?page=${pagina}&size=15`), [pagina])
  const historico = useDados(carregar)

  return (
    <>
      <div className="cabecalho">
        <h1>Estoque</h1>
      </div>

      {ehAdmin && (
        <FormularioMovimentacao
          aoRegistrar={() => {
            setPagina(0)
            historico.recarregar()
          }}
        />
      )}

      <section className="bloco bloco--tabela" aria-labelledby="titulo-movimentacoes">
        <h2 id="titulo-movimentacoes">Movimentações</h2>
        <Aviso>{historico.erro}</Aviso>
        <div className="tabela-rolagem">
          <table className="tabela">
            <thead>
              <tr>
                <th scope="col">Data</th>
                <th scope="col">Produto</th>
                <th scope="col" className="num">Quantidade</th>
                <th scope="col">Motivo</th>
                <th scope="col">Responsável</th>
              </tr>
            </thead>
            <tbody>
              {historico.dados?.conteudo.map((m) => (
                <tr key={m.id}>
                  <td>{formatarDataHora(m.data)}</td>
                  <td>{m.produtoNome}</td>
                  <td className="num">
                    <span className={m.tipo === 'ENTRADA' ? 'mov mov--entrada' : 'mov mov--saida'}>
                      {m.tipo === 'ENTRADA' ? '+' : '−'}
                      {formatarNumero(m.quantidade)}
                    </span>
                  </td>
                  <td>{m.motivo}</td>
                  <td>{m.usuarioNome}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        {historico.dados?.conteudo.length === 0 && <p className="vazio">Nenhuma movimentação registrada ainda.</p>}
        <Paginacao pagina={historico.dados} aoMudar={setPagina} />
      </section>
    </>
  )
}

function FormularioMovimentacao({ aoRegistrar }) {
  const carregarProdutos = useCallback(() => api('/produtos?size=100&sort=nome'), [])
  const produtos = useDados(carregarProdutos)
  const [produtoId, setProdutoId] = useState('')
  const [tipo, setTipo] = useState('ENTRADA')
  const [quantidade, setQuantidade] = useState('')
  const [motivo, setMotivo] = useState('')
  const [erro, setErro] = useState('')
  const [sucesso, setSucesso] = useState('')
  const [salvando, setSalvando] = useState(false)

  const selecionado = produtos.dados?.conteudo.find((p) => p.id === Number(produtoId))

  async function registrar(evento) {
    evento.preventDefault()
    setErro('')
    setSucesso('')
    setSalvando(true)
    try {
      const mov = await api('/estoque/movimentacoes', {
        method: 'POST',
        body: { produtoId: Number(produtoId), tipo, quantidade: Number(quantidade), motivo: motivo.trim() },
      })
      setSucesso(
        `${mov.tipo === 'ENTRADA' ? 'Entrada' : 'Saída'} de ${mov.quantidade} un. registrada para ${mov.produtoNome}.`,
      )
      setQuantidade('')
      setMotivo('')
      produtos.recarregar()
      aoRegistrar()
    } catch (falha) {
      setErro(falha.message)
    } finally {
      setSalvando(false)
    }
  }

  return (
    <section className="bloco" aria-labelledby="titulo-movimentar">
      <h2 id="titulo-movimentar">Registrar entrada ou saída</h2>
      <form className="formulario" onSubmit={registrar}>
        <fieldset className="alternador">
          <legend className="sr-only">Tipo de movimentação</legend>
          <label>
            <input type="radio" name="tipo" value="ENTRADA" checked={tipo === 'ENTRADA'} onChange={() => setTipo('ENTRADA')} />
            Entrada
          </label>
          <label>
            <input type="radio" name="tipo" value="SAIDA" checked={tipo === 'SAIDA'} onChange={() => setTipo('SAIDA')} />
            Saída
          </label>
        </fieldset>
        <div className="formulario--linha">
          <label className="campo campo--largo">
            Produto
            <select required value={produtoId} onChange={(e) => setProdutoId(e.target.value)}>
              <option value="">Escolha um produto</option>
              {produtos.dados?.conteudo.map((p) => (
                <option key={p.id} value={p.id}>{p.nome} ({p.sku})</option>
              ))}
            </select>
          </label>
          <label className="campo campo--curto">
            Quantidade
            <input required type="number" min="1" step="1" value={quantidade} onChange={(e) => setQuantidade(e.target.value)} />
          </label>
        </div>
        {selecionado && (
          <p className="dica">Saldo atual de {selecionado.nome}: {formatarNumero(selecionado.quantidadeEstoque)} un.</p>
        )}
        <label className="campo">
          Motivo
          <input
            required
            maxLength={200}
            placeholder={tipo === 'ENTRADA' ? 'Ex.: compra do fornecedor, NF 1234' : 'Ex.: avaria, validade vencida'}
            value={motivo}
            onChange={(e) => setMotivo(e.target.value)}
          />
        </label>
        <Aviso>{erro}</Aviso>
        <Aviso tipo="sucesso">{sucesso}</Aviso>
        <div className="formulario__acoes formulario__acoes--inicio">
          <button className="botao botao--primario" disabled={salvando}>
            {salvando ? 'Registrando…' : tipo === 'ENTRADA' ? 'Registrar entrada' : 'Registrar saída'}
          </button>
        </div>
      </form>
    </section>
  )
}
