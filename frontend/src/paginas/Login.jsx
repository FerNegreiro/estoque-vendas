import { useState } from 'react'
import Aviso from '../componentes/Aviso.jsx'
import Marca from '../componentes/Marca.jsx'
import { useAuth } from '../contexto/AuthContext.jsx'

export default function Login() {
  const { entrar } = useAuth()
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [erro, setErro] = useState('')
  const [enviando, setEnviando] = useState(false)

  async function enviar(evento) {
    evento.preventDefault()
    setErro('')
    setEnviando(true)
    try {
      await entrar(email.trim(), senha)
    } catch (falha) {
      setErro(falha.message)
      setEnviando(false)
    }
  }

  return (
    <main className="login">
      <div className="login__caixa">
        <Marca />
        <h1>Entre para registrar vendas e acompanhar o estoque.</h1>
        <form className="formulario" onSubmit={enviar}>
          <label className="campo">
            E-mail
            <input
              type="email"
              autoComplete="username"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </label>
          <label className="campo">
            Senha
            <input
              type="password"
              autoComplete="current-password"
              required
              value={senha}
              onChange={(e) => setSenha(e.target.value)}
            />
          </label>
          <Aviso>{erro}</Aviso>
          <button className="botao botao--primario botao--cheio" disabled={enviando}>
            {enviando ? 'Entrando…' : 'Entrar'}
          </button>
        </form>
      </div>
    </main>
  )
}
