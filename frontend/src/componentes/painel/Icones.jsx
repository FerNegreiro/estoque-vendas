const base = {
  viewBox: '0 0 24 24',
  fill: 'none',
  stroke: 'currentColor',
  strokeWidth: 2,
  strokeLinecap: 'round',
  strokeLinejoin: 'round',
  'aria-hidden': true,
}

export function IconeDinheiro() {
  return (
    <svg {...base}>
      <rect x="2.5" y="6" width="19" height="12" rx="2" />
      <circle cx="12" cy="12" r="2.8" />
      <path d="M6 9.5v5M18 9.5v5" />
    </svg>
  )
}

export function IconeDeposito() {
  return (
    <svg {...base}>
      <path d="M3 10.5 12 4l9 6.5V20a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1z" />
      <path d="M12 10.5v7M14.2 12.2c-.4-.6-1.2-.9-2.2-.9-1.3 0-2 .6-2 1.4 0 1.9 4.4 1 4.4 3 0 .8-.8 1.4-2.2 1.4-1 0-1.9-.4-2.3-1" />
    </svg>
  )
}

export function IconeRuptura() {
  return (
    <svg {...base}>
      <circle cx="12" cy="12" r="9" />
      <path d="m9 9 6 6M15 9l-6 6" />
    </svg>
  )
}

export function IconeGiro() {
  return (
    <svg {...base}>
      <path d="M20 11a8 8 0 0 0-14.3-4.9L4 8" />
      <path d="M4 3.5V8h4.5" />
      <path d="M4 13a8 8 0 0 0 14.3 4.9L20 16" />
      <path d="M20 20.5V16h-4.5" />
    </svg>
  )
}

export function IconeCaixa() {
  return (
    <svg {...base} strokeWidth={1.6}>
      <path d="M12 3 20 7.5v9L12 21l-8-4.5v-9z" />
      <path d="m4 7.5 8 4.5 8-4.5M12 12v9" />
    </svg>
  )
}
