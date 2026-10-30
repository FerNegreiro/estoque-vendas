const FORMAS = {
  A: <circle cx="6" cy="6" r="5" />,
  B: <polygon points="6,1 11.2,11 0.8,11" />,
  C: <polygon points="6,0.6 11.4,6 6,11.4 0.6,6" />,
}

export default function SeloAbc({ classe }) {
  return (
    <span className={`abc abc--${classe.toLowerCase()}`} title={`Classe ${classe}`}>
      <svg viewBox="0 0 12 12" aria-hidden="true">{FORMAS[classe]}</svg>
      {classe}
    </span>
  )
}
