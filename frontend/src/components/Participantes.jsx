import { useEffect, useState } from 'react';
import { api } from '../services/api.js';
import Feedback from './Feedback.jsx';
const statusTexto = {
  PENDENTE: 'Aguardando resposta',
  ACEITO: 'Aceito',
  RECUSADO: 'Recusado',
};
export default function Participantes({ grupoId, administrador, onAlterados }) {
  const [participantes, setParticipantes] = useState(null);
  const [convites, setConvites] = useState([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [busy, setBusy] = useState(false);
  const [retry, setRetry] = useState(0);
  const [remocao, setRemocao] = useState(null);
  useEffect(() => {
    const controller = new AbortController();
    Promise.all([
      api.listarParticipantes(grupoId, controller.signal),
      administrador
        ? api.convitesEnviados(grupoId, controller.signal)
        : Promise.resolve([]),
    ])
      .then(([p, c]) => {
        setParticipantes(p);
        setConvites(c);
        onAlterados();
      })
      .catch((err) => {
        if (err.name !== 'AbortError') setError(err.message);
      });
    return () => controller.abort();
  }, [grupoId, administrador, retry, onAlterados]);
  async function convidar(event) {
    event.preventDefault();
    if (busy) return;
    const form = event.currentTarget;
    const email = new FormData(form).get('email').trim();
    setBusy(true);
    setError('');
    setSuccess('');
    try {
      const convite = await api.convidar(grupoId, email);
      setConvites((c) => [convite, ...c.filter((v) => v.id !== convite.id)]);
      setSuccess(
        'Convite enviado. A pessoa poderá aceitar ou recusar na conta dela.',
      );
      form.reset();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }
  async function remover() {
    if (busy || !remocao) return;
    setBusy(true);
    setError('');
    setSuccess('');
    try {
      await api.removerParticipante(grupoId, remocao.id);
      setParticipantes((p) => p.filter((v) => v.id !== remocao.id));
      onAlterados();
      setSuccess(
        `${remocao.nome} foi removido e não tem mais acesso ao grupo.`,
      );
      setRemocao(null);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }
  return (
    <section className="card" aria-labelledby="participantes-titulo">
      <span className="eyebrow">A TURMA</span>
      <h2 id="participantes-titulo">
        Participantes{' '}
        {participantes && <span className="count">{participantes.length}</span>}
      </h2>
      {participantes === null ? (
        <p role="status">
          {error
            ? 'Não foi possível carregar a lista.'
            : 'Carregando participantes…'}
        </p>
      ) : (
        <ul className="participants">
          {participantes.map((p) => (
            <li key={p.id}>
              <span className="avatar" aria-hidden="true">
                {p.nome.slice(0, 1).toUpperCase()}
              </span>
              <span className="participant-name">
                {p.nome}
                {p.administrador && <small>Administrador</small>}
              </span>
              {administrador && !p.administrador && (
                <button
                  className="remove"
                  disabled={busy}
                  onClick={() => setRemocao(p)}
                  aria-label={`Remover ${p.nome}`}
                >
                  Remover
                </button>
              )}
            </li>
          ))}
        </ul>
      )}
      {remocao && (
        <div
          className="confirmation"
          role="alertdialog"
          aria-labelledby="remover-titulo"
          aria-describedby="remover-texto"
        >
          <h3 id="remover-titulo">Remover {remocao.nome}?</h3>
          <p id="remover-texto">Essa pessoa perderá o acesso ao grupo.</p>
          <div className="actions">
            <button
              className="secondary"
              disabled={busy}
              onClick={() => setRemocao(null)}
            >
              Cancelar
            </button>
            <button className="remove" disabled={busy} onClick={remover}>
              Confirmar remoção
            </button>
          </div>
        </div>
      )}
      {administrador && (
        <>
          <form onSubmit={convidar} aria-busy={busy}>
            <label htmlFor="convite-email">Convidar por e-mail</label>
            <input
              id="convite-email"
              name="email"
              type="email"
              required
              maxLength={254}
              placeholder="amigo@exemplo.com"
              disabled={busy}
            />
            <small>
              A pessoa precisa ter uma conta. O convite aparece dentro do
              Rachaí.
            </small>
            <button className="secondary invite-button" disabled={busy}>
              {busy ? 'Aguarde…' : 'Enviar convite'}
            </button>
          </form>
          {!!convites.length && (
            <div className="sent-invites">
              <h3>Convites enviados</h3>
              <ul>
                {convites.map((c) => (
                  <li key={c.id}>
                    <span>{c.destinatarioEmail}</span>
                    <small>{statusTexto[c.status]}</small>
                  </li>
                ))}
              </ul>
            </div>
          )}
        </>
      )}
      <Feedback error={error} success={success} />
      <button
        className="text-button refresh-list"
        disabled={busy}
        onClick={() => {
          setError('');
          setRetry((n) => n + 1);
        }}
      >
        Atualizar participantes
      </button>
    </section>
  );
}
