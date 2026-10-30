import { useCallback, useState } from 'react'
import Aviso from '../componentes/Aviso.jsx'
import Modal from '../componentes/Modal.jsx'
import { api } from '../servicos/api.js'
import { useDados } from '../servicos/useDados.js'

export default function Usuarios() {
  const carregar = useCallback(() => api('/usuarios'), [])
  const { dados, erro, recarregar } = useDados(carregar)
  const [criando, setCriando] = useState(false)

  return (
    <>
      <div className="cabecalho">
        <h1>Usuários</h1>
        <button type="button" className="botao botao--primario" onClick={() => setCriando(true)}>
          Cadastrar usuário
        </button>
      </div>
      <Aviso>{erro}</Aviso>
      <div className="bloco bloco--tabela">
        <div className="tabela-rolagem">
          <table className="tabela">
            <thead>
              <tr>
                <th scope="col">Nome</th>
                <th scope="col">E-mail</th>
                <th scope="col">Perfil</th>
                <th scope="col">Situação</th>
              </tr>
            </thead>
            <tbody>
              {dados?.map((u) => (
                <tr key={u.id}>
                  <td>{u.nome}</td>
                  <td>{u.email}</td>
                  <td>{u.perfil === 'ADMIN' ? 'Administrador' : 'Vendedor'}</td>
                  <td>{u.ativo ? 'Ativo' : 'Inativo'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
      {criando && (
        <FormularioUsuario
          aoFechar={() => setCriando(false)}
          aoSalvar={() => {
            setCriando(false)
            recarregar()
          }}
        />
      )}
    </>
  )
}

function FormularioUsuario({ aoFechar, aoSalvar }) {
  const [campos, setCampos] = useState({ nome: '', email: '', senha: '', perfil: 'VENDEDOR' })
  const [erro, setErro] = useState('')
  const [detalhes, setDetalhes] = useState([])
  const [salvando, setSalvando] = useState(false)
  const alterar = (campo) => (e) => setCampos((atual) => ({ ...atual, [campo]: e.target.value }))

  async function salvar(evento) {
    evento.preventDefault()
    setErro('')
    setDetalhes([])
    setSalvando(true)
    try {
      await api('/usuarios', { method: 'POST', body: campos })
      aoSalvar()
    } catch (falha) {
      setErro(falha.message)
      setDetalhes(falha.detalhes ?? [])
      setSalvando(false)
    }
  }

  return (
    <Modal titulo="Cadastrar usuário" aoFechar={aoFechar} largura="estreita">
      <form className="formulario" onSubmit={salvar}>
        <label className="campo">
          Nome
          <input required maxLength={120} value={campos.nome} onChange={alterar('nome')} />
        </label>
        <label className="campo">
          E-mail
          <input required type="email" maxLength={160} value={campos.email} onChange={alterar('email')} />
        </label>
        <label className="campo">
          Senha provisória
          <input
            required
            type="password"
            minLength={8}
            maxLength={72}
            autoComplete="new-password"
            value={campos.senha}
            onChange={alterar('senha')}
          />
          <span className="dica">Mínimo de 8 caracteres, com letras e números.</span>
        </label>
        <label className="campo">
          Perfil
          <select value={campos.perfil} onChange={alterar('perfil')}>
            <option value="VENDEDOR">Vendedor: registra vendas e consulta</option>
            <option value="ADMIN">Administrador: acesso completo</option>
          </select>
        </label>
        <Aviso>{erro}</Aviso>
        {detalhes.length > 0 && (
          <ul className="detalhes-erro">
            {detalhes.map((d) => <li key={d}>{d}</li>)}
          </ul>
        )}
        <div className="formulario__acoes">
          <button type="button" className="botao" onClick={aoFechar}>Cancelar</button>
          <button className="botao botao--primario" disabled={salvando}>
            {salvando ? 'Salvando…' : 'Cadastrar usuário'}
          </button>
        </div>
      </form>
    </Modal>
  )
}
