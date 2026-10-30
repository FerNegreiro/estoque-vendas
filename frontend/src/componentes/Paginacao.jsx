export default function Paginacao({ pagina, aoMudar }) {
  if (!pagina || pagina.totalPaginas <= 1) return null
  return (
    <div className="paginacao">
      <button
        type="button"
        className="botao botao--discreto"
        disabled={pagina.pagina === 0}
        onClick={() => aoMudar(pagina.pagina - 1)}
      >
        Anterior
      </button>
      <span>
        Página {pagina.pagina + 1} de {pagina.totalPaginas}
      </span>
      <button
        type="button"
        className="botao botao--discreto"
        disabled={pagina.pagina + 1 >= pagina.totalPaginas}
        onClick={() => aoMudar(pagina.pagina + 1)}
      >
        Próxima
      </button>
    </div>
  )
}
