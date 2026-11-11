import { useEffect, useState } from 'react'
import { useAuth0 } from '@auth0/auth0-react'

const defaultApiUrl = import.meta.env.VITE_API_URL || '/api'

function decodeJwtRoles(token) {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    const roles = []
    for (const [key, value] of Object.entries(payload)) {
      const lower = key.toLowerCase()
      if ((lower.endsWith('/roles') || lower === 'roles' || lower === 'permissions') && Array.isArray(value)) {
        roles.push(...value)
      }
    }
    return [...new Set(roles)]
  } catch {
    return []
  }
}

export default function App() {
  const {
    loginWithRedirect,
    logout,
    user,
    isAuthenticated,
    isLoading,
    getAccessTokenSilently
  } = useAuth0()

  const [apiUrl] = useState(defaultApiUrl)
  const [token, setToken] = useState('')
  const [roles, setRoles] = useState([])
  const [avaliacoes, setAvaliacoes] = useState([])
  const [carregandoLista, setCarregandoLista] = useState(false)
  const [enviando, setEnviando] = useState(false)
  const [erro, setErro] = useState('')
  const [sucesso, setSucesso] = useState('')

  const [emailAvaliador, setEmailAvaliador] = useState('')
  const [emailAvaliado, setEmailAvaliado] = useState('')
  const [nota, setNota] = useState(10)
  const [comentarios, setComentarios] = useState('')

  useEffect(() => {
    if (user?.email) {
      setEmailAvaliador(user.email)
    }
  }, [user])

  useEffect(() => {
    async function initAuthAndFetch() {
      if (!isAuthenticated) return
      try {
        const accessToken = await getAccessTokenSilently()
        setToken(accessToken)
        setRoles(decodeJwtRoles(accessToken))
        await buscarAvaliacoes(accessToken)
      } catch (err) {
        setErro('Erro ao obter token do Auth0: ' + (err.message || err))
      }
    }
    initAuthAndFetch()
  }, [isAuthenticated, getAccessTokenSilently])

  async function buscarAvaliacoes(overrideToken) {
    const authToken = overrideToken || token
    if (!authToken) return
    setCarregandoLista(true)
    setErro('')
    try {
      const response = await fetch(`${apiUrl}/avaliacoes`, {
        method: 'GET',
        headers: {
          Authorization: `Bearer ${authToken}`
        }
      })
      if (!response.ok) {
        if (response.status === 403) {
          throw new Error('Acesso negado (403): você não possui permissão para listar avaliações.')
        }
        throw new Error(`Erro ao listar avaliações (Status ${response.status})`)
      }
      const data = await response.json()
      setAvaliacoes(Array.isArray(data) ? data : [])
    } catch (err) {
      setErro(err.message || 'Erro de conexão com a API.')
    } finally {
      setCarregandoLista(false)
    }
  }

  async function cadastrarAvaliacao(e) {
    e.preventDefault()
    setErro('')
    setSucesso('')
    setEnviando(true)

    try {
      const authToken = token || (await getAccessTokenSilently())
      const response = await fetch(`${apiUrl}/avaliacoes`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${authToken}`
        },
        body: JSON.stringify({
          emailAvaliador: emailAvaliador || user?.email,
          emailAvaliado,
          comentarios,
          nota: Number(nota)
        })
      })

      if (response.status === 403) {
        throw new Error(
          'Acesso negado (403 Forbidden): Apenas usuários com a role ADMIN podem cadastrar avaliações.'
        )
      }
      if (!response.ok) {
        const text = await response.text()
        throw new Error(`Falha ao cadastrar avaliação (${response.status}): ${text}`)
      }

      setSucesso('Avaliação cadastrada com sucesso!')
      setEmailAvaliado('')
      setComentarios('')
      setNota(10)
      await buscarAvaliacoes(authToken)
    } catch (err) {
      setErro(err.message || 'Erro ao cadastrar avaliação.')
    } finally {
      setEnviando(false)
    }
  }

  if (isLoading) {
    return (
      <div className="container center-card">
        <h2>Carregando autenticação Auth0...</h2>
      </div>
    )
  }

  if (!isAuthenticated) {
    return (
      <div className="container center-card">
        <h1>Sistema de Avaliações</h1>
        <p className="subtitle">
          Plataforma corporativa para avaliação de colaboradores autenticada via Auth0.
        </p>
        <button className="btn-primary" onClick={() => loginWithRedirect()}>
          Entrar com Auth0
        </button>
      </div>
    )
  }

  return (
    <div className="container">
      <header className="header">
        <div>
          <h1>Sistema de Avaliações</h1>
          <p className="user-info">
            Logado como: <strong>{user?.email || user?.name}</strong>
            {roles.length > 0 ? (
              roles.map((r) => (
                <span key={r} className={`badge ${r.toUpperCase() === 'ADMIN' ? 'badge-admin' : 'badge-user'}`}>
                  {r}
                </span>
              ))
            ) : (
              <span className="badge badge-user">USER</span>
            )}
          </p>
        </div>
        <button
          className="btn-secondary"
          onClick={() => logout({ logoutParams: { returnTo: window.location.origin } })}
        >
          Sair
        </button>
      </header>

      {erro && <div className="alert alert-error">{erro}</div>}
      {sucesso && <div className="alert alert-success">{sucesso}</div>}

      <div className="grid">
        <section className="card">
          <h2>Nova Avaliação (ADMIN)</h2>
          <form onSubmit={cadastrarAvaliacao} className="form">
            <label>
              E-mail do Avaliador
              <input
                type="email"
                value={emailAvaliador}
                onChange={(e) => setEmailAvaliador(e.target.value)}
                placeholder="avaliador@empresa.com"
                required
              />
            </label>

            <label>
              E-mail do Avaliado
              <input
                type="email"
                value={emailAvaliado}
                onChange={(e) => setEmailAvaliado(e.target.value)}
                placeholder="colaborador@empresa.com"
                required
              />
            </label>

            <label>
              Nota (0 a 10)
              <input
                type="number"
                min="0"
                max="10"
                value={nota}
                onChange={(e) => setNota(e.target.value)}
                required
              />
            </label>

            <label>
              Comentários da Avaliação
              <textarea
                rows="4"
                value={comentarios}
                onChange={(e) => setComentarios(e.target.value)}
                placeholder="Descreva os pontos fortes e oportunidades de melhoria..."
                required
              />
            </label>

            <button type="submit" className="btn-primary" disabled={enviando}>
              {enviando ? 'Cadastrando...' : 'Cadastrar Avaliação'}
            </button>
          </form>
        </section>

        <section className="card">
          <div className="list-header">
            <h2>Avaliações Registradas ({avaliacoes.length})</h2>
            <button
              type="button"
              className="btn-secondary"
              onClick={() => buscarAvaliacoes()}
              disabled={carregandoLista}
            >
              {carregandoLista ? 'Atualizando...' : 'Atualizar'}
            </button>
          </div>

          {avaliacoes.length === 0 ? (
            <p className="empty-state">Nenhuma avaliação cadastrada até o momento.</p>
          ) : (
            <div className="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Avaliador</th>
                    <th>Avaliado</th>
                    <th>Nota</th>
                    <th>Comentários</th>
                    <th>Data</th>
                  </tr>
                </thead>
                <tbody>
                  {avaliacoes.map((av) => (
                    <tr key={av.id}>
                      <td>#{av.id}</td>
                      <td>{av.emailAvaliador}</td>
                      <td>{av.emailAvaliado}</td>
                      <td>
                        <span className="score-pill">{av.nota} / 10</span>
                      </td>
                      <td>{av.comentarios}</td>
                      <td>
                        {av.dataAvaliacao
                          ? new Date(av.dataAvaliacao).toLocaleString('pt-BR')
                          : '-'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </div>
    </div>
  )
}
