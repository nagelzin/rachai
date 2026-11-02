import { useCallback, useEffect, useState } from 'react';
import { api } from '../services/api.js';
import Feedback from '../components/Feedback.jsx';

export default function Inicio({ onOpen, usuario }) {
  const [grupos, setGrupos] = useState(null);
  const [convites, setConvites] = useState([]);
  const [error, setError] = useState('');
  const [loadError, setLoadError] = useState('');
  const [success, setSuccess] = useState('');
  const [busy, setBusy] = useState(false);
  const [respondendo, setRespondendo] = useState(null);
  const [atualizando, setAtualizando] = useState(true);
  const carregar = useCallback((signal) => {
    return Promise.all([
      api.listarGrupos(signal),
      api.convitesRecebidos(signal),
    ])
      .then(([g, c]) => {
        setGrupos(g);
        setConvites(c);
        setLoadError('');
      })
      .catch((err) => {
        if (err.name !== 'AbortError') setLoadError(err.message);
      })
      .finally(() => {
        if (!signal?.aborted) setAtualizando(false);
      });
  }, []);
  useEffect(() => {
    const controller = new AbortController();
    carregar(controller.signal);
    const atualizar = () => {
      setAtualizando(true);
      carregar(controller.signal);
    };
    window.addEventListener('focus', atualizar);
    return () => {
      controller.abort();
      window.removeEventListener('focus', atualizar);
    };
  }, [carregar]);
  async function criar(event) {
    event.preventDefault();
    if (busy) return;
    const fields = new FormData(event.currentTarget);
    const nome = fields.get('nome').trim();
    if (!nome) {
      setError('Informe o nome do grupo.');
      return;
    }
    setBusy(true);
    setError('');
    try {
      const grupo = await api.criarGrupo({
        nome,
        descricao: fields.get('descricao').trim(),
      });
      onOpen(grupo.id);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }
  async function responder(convite, aceitar) {
    if (respondendo) return;
    setRespondendo(convite.id);
    setLoadError('');
    setSuccess('');
    try {
      await api.responderConvite(convite.id, aceitar);
      setSuccess(
        aceitar ? `Você entrou em ${convite.grupoNome}.` : 'Convite recusado.',
      );
      await carregar();
    } catch (err) {
      setLoadError(err.message);
    } finally {
      setRespondendo(null);
    }
  }
  return (
    <main className="content dashboard">
      <section className="dashboard-heading">
        <div>
          <span className="eyebrow">SEUS ENCONTROS</span>
          <h1>Meus grupos</h1>
          <p>
            Olá, {usuario.nome}. Organize o próximo encontro com a sua turma.
          </p>
        </div>
        <button
          className="secondary"
          disabled={atualizando}
          onClick={() => {
            setAtualizando(true);
            carregar();
          }}
        >
          {atualizando ? 'Atualizando…' : 'Atualizar grupos e convites'}
        </button>
      </section>
      <Feedback error={loadError} success={success} />
      <div className="dashboard-grid">
        <div className="dashboard-lists">
          <section aria-labelledby="convites-titulo" className="card">
            <span className="eyebrow">VAMOS JUNTOS?</span>
            <h2 id="convites-titulo">
              Convites recebidos{' '}
              <span className="count">{convites.length}</span>
            </h2>
            {!convites.length ? (
              <p className="empty-state">
                Nenhum convite pendente. Peça ao administrador para convidar seu
                e-mail: <strong>{usuario.email}</strong>.
              </p>
            ) : (
              <ul className="invitation-list">
                {convites.map((c) => (
                  <li key={c.id}>
                    <h3>{c.grupoNome}</h3>
                    <p>{c.administradorNome} convidou você para participar.</p>
                    <div className="actions">
                      <button
                        className="secondary"
                        disabled={!!respondendo}
                        onClick={() => responder(c, true)}
                      >
                        Aceitar convite
                      </button>
                      <button
                        className="text-button"
                        disabled={!!respondendo}
                        onClick={() => responder(c, false)}
                      >
                        Recusar
                      </button>
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </section>
          <section aria-labelledby="grupos-titulo">
            <h2 id="grupos-titulo">Grupos dos quais participo</h2>
            {grupos === null ? (
              <p role="status">
                {loadError
                  ? 'Não foi possível carregar seus grupos.'
                  : 'Carregando grupos…'}
              </p>
            ) : !grupos.length ? (
              <p className="empty-state">
                Você ainda não participa de nenhum grupo. Crie o primeiro ou
                aceite um convite.
              </p>
            ) : (
              <ul className="group-list">
                {grupos.map((g) => (
                  <li key={g.id}>
                    <a className="card group-link" href={`#grupo/${g.id}`}>
                      <span className="eyebrow">
                        {g.usuarioResponsavelId === usuario.id
                          ? 'ADMINISTRADOR'
                          : 'MEMBRO'}
                      </span>
                      <h3>{g.nome}</h3>
                      <p>
                        {g.descricao || `Organizado por ${g.responsavelNome}`}
                      </p>
                      <span className="link-label">Abrir grupo →</span>
                    </a>
                  </li>
                ))}
              </ul>
            )}
          </section>
        </div>
        <section className="card create-group">
          <span className="eyebrow">UM NOVO ENCONTRO</span>
          <h2>Criar grupo</h2>
          <p>Você será o administrador e poderá convidar outras pessoas.</p>
          <form onSubmit={criar} aria-busy={busy}>
            <label htmlFor="nome">Nome do grupo</label>
            <input
              id="nome"
              name="nome"
              required
              maxLength={120}
              placeholder="Ex.: Churrasco de domingo"
              disabled={busy}
            />
            <label htmlFor="descricao">
              Descrição <span className="optional">(opcional)</span>
            </label>
            <textarea
              id="descricao"
              name="descricao"
              maxLength={500}
              rows={3}
              disabled={busy}
            />
            <Feedback error={error} />
            <button className="primary" disabled={busy}>
              {busy ? 'Criando grupo…' : 'Criar grupo →'}
            </button>
          </form>
        </section>
      </div>
    </main>
  );
}
