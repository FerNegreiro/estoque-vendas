const BASE_URL = import.meta.env?.VITE_API_URL ?? '/api'
const CHAVE_SESSAO = 'balcao.sessao'

export class ErroApi extends Error {
  constructor(mensagem, status, detalhes = []) {
    super(mensagem)
    this.status = status
    this.detalhes = detalhes
  }
}

export function lerSessao() {
  try {
    const sessao = JSON.parse(sessionStorage.getItem(CHAVE_SESSAO))
    if (!sessao || Date.now() > sessao.expiraEm) return null
    return sessao
  } catch {
    return null
  }
}

export function salvarSessao({ token, expiraEmSegundos, usuario }) {
  const expiraEm = Date.now() + expiraEmSegundos * 1000
  sessionStorage.setItem(CHAVE_SESSAO, JSON.stringify({ token, usuario, expiraEm }))
}

export function limparSessao() {
  sessionStorage.removeItem(CHAVE_SESSAO)
}

export async function api(caminho, { method = 'GET', body } = {}) {
  const sessao = lerSessao()
  const headers = { Accept: 'application/json' }
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  if (sessao) headers.Authorization = `Bearer ${sessao.token}`

  let resposta
  try {
    resposta = await fetch(`${BASE_URL}${caminho}`, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
    })
  } catch {
    throw new ErroApi('Sem conexão com o servidor. Confira se a API está rodando.', 0)
  }

  if (resposta.status === 401 && sessao) {
    limparSessao()
    window.dispatchEvent(new Event('sessao-expirada'))
  }
  if (!resposta.ok) {
    const erro = await resposta.json().catch(() => ({}))
    throw new ErroApi(erro.mensagem ?? 'Não foi possível concluir a operação.', resposta.status, erro.detalhes ?? [])
  }
  return resposta.status === 204 ? null : resposta.json()
}
