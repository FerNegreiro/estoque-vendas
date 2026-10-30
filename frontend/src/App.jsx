import { useEffect, useState } from 'react'
import Layout from './componentes/Layout.jsx'
import { useAuth } from './contexto/AuthContext.jsx'
import Estoque from './paginas/Estoque.jsx'
import Login from './paginas/Login.jsx'
import Painel from './paginas/Painel.jsx'
import Produtos from './paginas/Produtos.jsx'
import Usuarios from './paginas/Usuarios.jsx'
import Vendas from './paginas/Vendas.jsx'

const ROTAS = {
  painel: { titulo: 'Painel', Pagina: Painel },
  vendas: { titulo: 'Vendas', Pagina: Vendas },
  produtos: { titulo: 'Produtos', Pagina: Produtos },
  estoque: { titulo: 'Estoque', Pagina: Estoque },
  usuarios: { titulo: 'Usuários', Pagina: Usuarios, somenteAdmin: true },
}

const rotaDaUrl = () => {
  const nome = window.location.hash.slice(1)
  return ROTAS[nome] ? nome : 'painel'
}

export default function App() {
  const { usuario, ehAdmin } = useAuth()
  const [rota, setRota] = useState(rotaDaUrl)

  useEffect(() => {
    const aoMudar = () => setRota(rotaDaUrl())
    window.addEventListener('hashchange', aoMudar)
    return () => window.removeEventListener('hashchange', aoMudar)
  }, [])

  useEffect(() => {
    document.title = `${ROTAS[rota].titulo} | Balcão`
  }, [rota])

  if (!usuario) return <Login />

  const permitida = !ROTAS[rota].somenteAdmin || ehAdmin
  const { Pagina } = permitida ? ROTAS[rota] : ROTAS.painel

  return (
    <Layout rotas={ROTAS} rotaAtiva={permitida ? rota : 'painel'}>
      <Pagina />
    </Layout>
  )
}
