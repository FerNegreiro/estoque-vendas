import { useCallback, useState } from 'react'
import Aviso from '../componentes/Aviso.jsx'
import Etiqueta from '../componentes/Etiqueta.jsx'
import Modal from '../componentes/Modal.jsx'
import Paginacao from '../componentes/Paginacao.jsx'
import { useAuth } from '../contexto/AuthContext.jsx'
import { api } from '../servicos/api.js'
import { formatarMoeda, formatarNumero, paraNumero } from '../servicos/formato.js'
import { useDados } from '../servicos/useDados.js'

export default function Produtos() {
  const { ehAdmin } = useAuth()
  const [busca, setBusca] = useState('')
  const [termo, setTermo] = useState('')
  const [pagina, setPagina] = useState(0)
  const carregar = useCallback(
    () => api(`/produtos?termo=${encodeURIComponent(termo)}&page=${pagina}&size=15&sort=nome`),
    [termo, pagina],
  )
  const { dados, erro, recarregar } = useDados(carregar)
  const [edicao, setEdicao] = useState(null)
  const [erroAcao, setErroAcao] = useState('')

  function buscar(evento) {
    evento.preventDefault()
    setPagina(0)
    setTermo(busca.trim())
  }

  async function desativar(produto) {
    const ok = window.confirm(
      `Desativar ${produto.nome}? Ele sai das listas e das vendas, mas o histórico continua guardado.`,
    )
    if (!ok) return
    setErroAcao('')
    try {
      await api(`/produtos/${produto.id}`, { method: 'DELETE' })
      recarregar()
    } catch (falha) {
      setErroAcao(falha.message)
    }
  }

  return (
    <>
      <div className="cabecalho">
        <h1>Produtos</h1>
        {ehAdmin && (
          <button type="button" className="botao botao--primario" onClick={() => setEdicao('novo')}>
            Cadastrar produto
          </button>
        )}
      </div>

      <form className="busca" role="search" onSubmit={buscar}>
        <label htmlFor="busca-produto" className="sr-only">Buscar produto</label>
        <input
          id="busca-produto"
          type="search"
          placeholder="Buscar por nome ou SKU"
          value={busca}
          onChange={(e) => setBusca(e.target.value)}
        />
        <button className="botao">Buscar</button>
      </form>

      <Aviso>{erro || erroAcao}</Aviso>

      <div className="bloco bloco--tabela">
        <div className="tabela-rolagem">
          <table className="tabela">
            <thead>
              <tr>
                <th scope="col">SKU</th>
                <th scope="col">Produto</th>
                <th scope="col" className="num">Preço</th>
                <th scope="col" className="num">Estoque</th>
                {ehAdmin && <th scope="col"><span className="sr-only">Ações</span></th>}
              </tr>
            </thead>
            <tbody>
              {dados?.conteudo.map((produto) => (
                <tr key={produto.id}>
                  <td><Etiqueta>{produto.sku}</Etiqueta></td>
                  <td>
                    <span className="tabela__principal">{produto.nome}</span>
                    <span className="tabela__secundario">{produto.categoriaNome}</span>
                  </td>
                  <td className="num">{formatarMoeda(produto.precoVenda)}</td>
                  <td className="num">
                    <span className={produto.estoqueBaixo ? 'saldo saldo--baixo' : 'saldo'}>
                      {formatarNumero(produto.quantidadeEstoque)}
                    </span>
                    {produto.estoqueBaixo && (
                      <span className="tabela__secundario">mínimo {produto.estoqueMinimo}</span>
                    )}
                  </td>
                  {ehAdmin && (
                    <td className="acoes">
                      <button type="button" className="botao botao--discreto" onClick={() => setEdicao(produto)}>
                        Editar
                      </button>
                      <button
                        type="button"
                        className="botao botao--discreto botao--perigo"
                        onClick={() => desativar(produto)}
                      >
                        Desativar
                      </button>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        {dados?.conteudo.length === 0 && (
          <p className="vazio">
            {termo ? `Nenhum produto encontrado para “${termo}”.` : 'Nenhum produto cadastrado ainda.'}
          </p>
        )}
        <Paginacao pagina={dados} aoMudar={setPagina} />
      </div>

      {edicao && (
        <FormularioProduto
          produto={edicao === 'novo' ? null : edicao}
          aoFechar={() => setEdicao(null)}
          aoSalvar={() => {
            setEdicao(null)
            recarregar()
          }}
        />
      )}
    </>
  )
}

const paraTexto = (valor) => (valor === null || valor === undefined ? '' : String(valor).replace('.', ','))

function FormularioProduto({ produto, aoFechar, aoSalvar }) {
  const carregarCategorias = useCallback(() => api('/categorias'), [])
  const categorias = useDados(carregarCategorias)
  const [campos, setCampos] = useState(() => ({
    sku: produto?.sku ?? '',
    nome: produto?.nome ?? '',
    categoriaId: produto?.categoriaId ?? '',
    precoCusto: paraTexto(produto?.precoCusto),
    precoVenda: paraTexto(produto?.precoVenda),
    estoqueMinimo: produto?.estoqueMinimo ?? 0,
    quantidadeInicial: 0,
    descricao: produto?.descricao ?? '',
  }))
  const [erro, setErro] = useState('')
  const [detalhes, setDetalhes] = useState([])
  const [salvando, setSalvando] = useState(false)

  const alterar = (campo) => (evento) => setCampos((atual) => ({ ...atual, [campo]: evento.target.value }))

  async function salvar(evento) {
    evento.preventDefault()
    setErro('')
    setDetalhes([])
    const precoCusto = paraNumero(campos.precoCusto)
    const precoVenda = paraNumero(campos.precoVenda)
    if (Number.isNaN(precoCusto) || Number.isNaN(precoVenda)) {
      setErro('Informe os preços como no exemplo 12,90.')
      return
    }
    const corpo = {
      sku: campos.sku.trim(),
      nome: campos.nome.trim(),
      descricao: campos.descricao.trim() || null,
      categoriaId: Number(campos.categoriaId),
      precoCusto,
      precoVenda,
      estoqueMinimo: Number(campos.estoqueMinimo),
      quantidadeInicial: produto ? null : Number(campos.quantidadeInicial),
    }
    setSalvando(true)
    try {
      await api(produto ? `/produtos/${produto.id}` : '/produtos', {
        method: produto ? 'PUT' : 'POST',
        body: corpo,
      })
      aoSalvar()
    } catch (falha) {
      setErro(falha.message)
      setDetalhes(falha.detalhes ?? [])
      setSalvando(false)
    }
  }

  return (
    <Modal titulo={produto ? `Editar ${produto.nome}` : 'Cadastrar produto'} aoFechar={aoFechar}>
      <form className="formulario" onSubmit={salvar}>
        <div className="grade-campos">
          <label className="campo">
            SKU
            <input required maxLength={40} pattern="[A-Za-z0-9\-]+" value={campos.sku} onChange={alterar('sku')} />
          </label>
          <label className="campo">
            Categoria
            <select required value={campos.categoriaId} onChange={alterar('categoriaId')}>
              <option value="">Escolha</option>
              {categorias.dados?.map((c) => (
                <option key={c.id} value={c.id}>{c.nome}</option>
              ))}
            </select>
          </label>
        </div>
        <label className="campo">
          Nome
          <input required maxLength={150} value={campos.nome} onChange={alterar('nome')} />
        </label>
        <div className="grade-campos">
          <label className="campo">
            Preço de custo (R$)
            <input required inputMode="decimal" placeholder="0,00" value={campos.precoCusto} onChange={alterar('precoCusto')} />
          </label>
          <label className="campo">
            Preço de venda (R$)
            <input required inputMode="decimal" placeholder="0,00" value={campos.precoVenda} onChange={alterar('precoVenda')} />
          </label>
          <label className="campo">
            Estoque mínimo
            <input required type="number" min="0" step="1" value={campos.estoqueMinimo} onChange={alterar('estoqueMinimo')} />
          </label>
          {!produto && (
            <label className="campo">
              Quantidade inicial
              <input type="number" min="0" step="1" value={campos.quantidadeInicial} onChange={alterar('quantidadeInicial')} />
            </label>
          )}
        </div>
        <label className="campo">
          Descrição (opcional)
          <textarea rows={2} maxLength={500} value={campos.descricao} onChange={alterar('descricao')} />
        </label>
        {produto && (
          <p className="dica">O saldo em estoque muda pela tela Estoque, para que toda alteração fique registrada.</p>
        )}
        <Aviso>{erro}</Aviso>
        {detalhes.length > 0 && (
          <ul className="detalhes-erro">
            {detalhes.map((d) => <li key={d}>{d}</li>)}
          </ul>
        )}
        <div className="formulario__acoes">
          <button type="button" className="botao" onClick={aoFechar}>Cancelar</button>
          <button className="botao botao--primario" disabled={salvando}>
            {salvando ? 'Salvando…' : produto ? 'Salvar alterações' : 'Cadastrar produto'}
          </button>
        </div>
      </form>
    </Modal>
  )
}
