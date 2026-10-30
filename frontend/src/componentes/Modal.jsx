import { useEffect, useRef } from 'react'

export default function Modal({ titulo, aoFechar, children, largura = 'media' }) {
  const caixa = useRef(null)
  const fechar = useRef(aoFechar)

  useEffect(() => {
    fechar.current = aoFechar
  })

  useEffect(() => {
    const focoAnterior = document.activeElement
    caixa.current?.querySelector('input, select, textarea, button')?.focus()
    const aoTeclar = (evento) => evento.key === 'Escape' && fechar.current()
    document.addEventListener('keydown', aoTeclar)
    return () => {
      document.removeEventListener('keydown', aoTeclar)
      focoAnterior?.focus?.()
    }
  }, [])

  return (
    <div className="modal" onMouseDown={(e) => e.target === e.currentTarget && fechar.current()}>
      <div
        ref={caixa}
        className={`modal__caixa modal__caixa--${largura}`}
        role="dialog"
        aria-modal="true"
        aria-labelledby="modal-titulo"
      >
        <div className="modal__topo">
          <h2 id="modal-titulo">{titulo}</h2>
          <button type="button" className="botao botao--discreto" onClick={() => fechar.current()} aria-label="Fechar">
            ✕
          </button>
        </div>
        {children}
      </div>
    </div>
  )
}
