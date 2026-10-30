import { useCallback, useEffect, useState } from 'react'

export function useDados(carregar) {
  const [estado, setEstado] = useState({ dados: null, erro: '', carregando: true })
  const [versao, setVersao] = useState(0)

  useEffect(() => {
    let ativo = true
    setEstado((anterior) => ({ ...anterior, carregando: true, erro: '' }))
    carregar()
      .then((dados) => ativo && setEstado({ dados, erro: '', carregando: false }))
      .catch((erro) => ativo && setEstado({ dados: null, erro: erro.message, carregando: false }))
    return () => {
      ativo = false
    }
  }, [carregar, versao])

  const recarregar = useCallback(() => setVersao((v) => v + 1), [])
  return { ...estado, recarregar }
}
