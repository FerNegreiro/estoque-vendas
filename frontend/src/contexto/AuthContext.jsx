import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { api, lerSessao, limparSessao, salvarSessao } from '../servicos/api.js'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(() => lerSessao()?.usuario ?? null)

  useEffect(() => {
    const aoExpirar = () => setUsuario(null)
    window.addEventListener('sessao-expirada', aoExpirar)
    return () => window.removeEventListener('sessao-expirada', aoExpirar)
  }, [])

  const entrar = useCallback(async (email, senha) => {
    const resposta = await api('/auth/login', { method: 'POST', body: { email, senha } })
    salvarSessao(resposta)
    setUsuario(resposta.usuario)
  }, [])

  const sair = useCallback(() => {
    limparSessao()
    setUsuario(null)
  }, [])

  const valor = useMemo(
    () => ({ usuario, entrar, sair, ehAdmin: usuario?.perfil === 'ADMIN' }),
    [usuario, entrar, sair],
  )
  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>
}

export const useAuth = () => useContext(AuthContext)
