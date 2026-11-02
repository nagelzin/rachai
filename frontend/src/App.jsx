import { useEffect, useState } from 'react';
import Inicio from './pages/Inicio.jsx';
import Grupo from './pages/Grupo.jsx';
import Conta from './pages/Conta.jsx';
import { api } from './services/api.js';
import Feedback from './components/Feedback.jsx';

const currentId = () =>
  window.location.hash.match(/^#grupo\/([1-9]\d*)$/)?.[1] ?? null;
function GrupoAberto({ id, usuario }) {
  const [grupo, setGrupo] = useState(null);
  const [error, setError] = useState('');
  const [retry, setRetry] = useState(0);
  useEffect(() => {
    const controller = new AbortController();
    api
      .consultarGrupo(id, controller.signal)
      .then(setGrupo)
      .catch((err) => {
        if (err.name !== 'AbortError') setError(err.message);
      });
    return () => controller.abort();
  }, [id, retry]);
  return (
    <main className="content group-content">
      <a className="back-link" href="#">
        ← Meus grupos
      </a>
      {error ? (
        <section className="card">
          <Feedback error={error} />
          <button
            className="secondary"
            onClick={() => {
              setError('');
              setRetry((n) => n + 1);
            }}
          >
            Tentar novamente
          </button>
        </section>
      ) : !grupo ? (
        <p role="status">Carregando grupo…</p>
      ) : (
        <Grupo grupo={grupo} usuario={usuario} />
      )}
    </main>
  );
}
export default function App() {
  const [id, setId] = useState(currentId);
  const [usuario, setUsuario] = useState(null);
  const [loading, setLoading] = useState(true);
  const [erro, setErro] = useState('');
  const [aviso, setAviso] = useState('');
  const [retry, setRetry] = useState(0);
  const [saindo, setSaindo] = useState(false);
  useEffect(() => {
    const navigate = () => setId(currentId());
    const expirar = () => {
      setUsuario(null);
      setAviso('Sua sessão terminou. Entre novamente para continuar.');
    };
    window.addEventListener('hashchange', navigate);
    window.addEventListener('sessao-expirada', expirar);
    return () => {
      window.removeEventListener('hashchange', navigate);
      window.removeEventListener('sessao-expirada', expirar);
    };
  }, []);
  useEffect(() => {
    const controller = new AbortController();
    api
      .eu(controller.signal)
      .then(setUsuario)
      .catch((err) => {
        if (err.name !== 'AbortError' && err.status !== 401)
          setErro(err.message);
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false);
      });
    return () => controller.abort();
  }, [retry]);
  useEffect(() => {
    const controller = new AbortController();
    const conferirConta = () => {
      api
        .eu(controller.signal)
        .then((conta) => {
          if (conta.id !== usuario?.id) window.location.reload();
        })
        .catch((err) => {
          if (err.status === 401 && usuario) {
            setUsuario(null);
            setAviso('Sua sessão terminou. Entre novamente para continuar.');
          }
        });
    };
    window.addEventListener('focus', conferirConta);
    return () => {
      controller.abort();
      window.removeEventListener('focus', conferirConta);
    };
  }, [usuario]);
  async function sair() {
    setSaindo(true);
    setErro('');
    try {
      await api.sair();
      setUsuario(null);
      setAviso('');
      window.location.hash = '';
    } catch (err) {
      setErro(err.message);
    } finally {
      setSaindo(false);
    }
  }
  return (
    <>
      <header className="site-header">
        <a href="#" className="brand" aria-label="Rachaí, início">
          <span className="brand-symbol" aria-hidden="true">
            r.
          </span>
          rachaí<span className="brand-dot">●</span>
        </a>
        {usuario ? (
          <nav className="account-nav" aria-label="Sua conta">
            <a href="#">Meus grupos</a>
            <span title={usuario.email}>{usuario.nome}</span>
            <button className="secondary" disabled={saindo} onClick={sair}>
              {saindo ? 'Saindo…' : 'Sair'}
            </button>
          </nav>
        ) : (
          <span className="header-note">
            Bons encontros. Contas organizadas.
          </span>
        )}
      </header>
      {erro ? (
        <main className="content">
          <Feedback error={erro} />
          <button
            className="secondary"
            onClick={() => {
              setErro('');
              setLoading(true);
              setRetry((n) => n + 1);
            }}
          >
            Tentar novamente
          </button>
        </main>
      ) : loading ? (
        <main className="content">
          <p role="status">Carregando sua conta…</p>
        </main>
      ) : !usuario ? (
        <Conta
          aviso={aviso}
          onEntrar={(u) => {
            setUsuario(u);
            setAviso('');
            setId(null);
            window.location.hash = '';
          }}
        />
      ) : id ? (
        <GrupoAberto key={`${usuario.id}-${id}`} id={id} usuario={usuario} />
      ) : (
        <Inicio
          key={usuario.id}
          usuario={usuario}
          onOpen={(groupId) => {
            window.location.hash = `grupo/${groupId}`;
          }}
        />
      )}
      <footer className="site-footer">
        Rachaí · Mais tempo para estar junto.
      </footer>
    </>
  );
}
