export default function Aviso({ tipo = 'erro', children }) {
  if (!children) return null
  return (
    <p className={`aviso aviso--${tipo}`} role={tipo === 'erro' ? 'alert' : 'status'}>
      {children}
    </p>
  )
}
