import { useAuth } from '../contexto/AuthContext.jsx'
import Marca from './Marca.jsx'

export default function Layout({ rotas, rotaAtiva, children }) {
  const { usuario, sair, ehAdmin } = useAuth()
  const itens = Object.entries(rotas).filter(([, rota]) => !rota.somenteAdmin || ehAdmin)

  return (
    <div className="app">
      <a className="pular" href="#conteudo">Pular para o conteúdo</a>
      <header className="trilho">
        <Marca />
        <nav aria-label="Principal" className="trilho__nav">
          <ul>
            {itens.map(([chave, rota]) => (
              <li key={chave}>
                <a href={`#${chave}`} aria-current={chave === rotaAtiva ? 'page' : undefined}>
                  {rota.titulo}
                </a>
              </li>
            ))}
          </ul>
        </nav>
        <div className="trilho__usuario">
          <span className="trilho__nome">{usuario.nome}</span>
          <span className="trilho__perfil">{ehAdmin ? 'Administrador' : 'Vendedor'}</span>
          <button type="button" className="botao botao--discreto" onClick={sair}>
            Sair
          </button>
        </div>
      </header>
      <main id="conteudo" className="conteudo" tabIndex={-1}>
        {children}
      </main>
    </div>
  )
}
