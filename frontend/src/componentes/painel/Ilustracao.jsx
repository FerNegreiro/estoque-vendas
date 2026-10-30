function caixa(cx, cy, largura, altura) {
  const p = (...pares) => pares.map(([x, y]) => `${x},${y}`).join(' ')
  const meia = largura / 2
  return {
    topo: p([cx, cy], [cx + largura, cy + meia], [cx, cy + largura], [cx - largura, cy + meia]),
    esquerda: p([cx - largura, cy + meia], [cx, cy + largura], [cx, cy + largura + altura], [cx - largura, cy + meia + altura]),
    direita: p([cx, cy + largura], [cx + largura, cy + meia], [cx + largura, cy + meia + altura], [cx, cy + largura + altura]),
    fita: p([cx - meia, cy + meia / 2], [cx + meia, cy + meia * 1.5], [cx + meia * 0.72, cy + meia * 1.64], [cx - meia * 1.28, cy + meia * 0.64]),
  }
}

const PILHA = [
  caixa(72, 92, 46, 40),
  caixa(150, 92, 46, 40),
  caixa(111, 40, 46, 40),
]

export default function Ilustracao() {
  return (
    <svg className="ilustracao" viewBox="0 0 220 190" aria-hidden="true">
      <ellipse cx="111" cy="176" rx="96" ry="10" className="ilustracao__sombra" />
      {PILHA.map((c, indice) => (
        <g key={indice}>
          <polygon points={c.esquerda} className="ilustracao__lado" />
          <polygon points={c.direita} className="ilustracao__frente" />
          <polygon points={c.topo} className="ilustracao__topo" />
          <polygon points={c.fita} className="ilustracao__fita" />
        </g>
      ))}
      <g transform="translate(150 150) skewY(-26.5)">
        <rect x="6" y="0" width="30" height="14" rx="1.5" className="ilustracao__etiqueta" />
        <path d="M10 3v8M13 3v8M15 3v8M19 3v8M21 3v8M25 3v8M28 3v8M31 3v8" className="ilustracao__barras" />
      </g>
    </svg>
  )
}
