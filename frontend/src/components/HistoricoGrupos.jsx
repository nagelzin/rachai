import { useEffect, useState } from 'react';
import { api } from '../services/api.js';
import Feedback from './Feedback.jsx';

export default function HistoricoGrupos() {
  const [ate, setAte] = useState('');
  const [tentativa, setTentativa] = useState(0);
  const [resultado, setResultado] = useState(null);
  const chave = `${ate}-${tentativa}`;
  const atual = resultado?.chave === chave ? resultado : null;
  useEffect(() => {
    const controller = new AbortController();
    api
      .historicoGrupos(ate, controller.signal)
      .then((dados) => {
        if (!controller.signal.aborted) setResultado({ chave, dados });
      })
      .catch((erro) => {
        if (!controller.signal.aborted)
          setResultado({ chave, erro: erro.message });
      });
    return () => controller.abort();
  }, [ate, chave]);
  return (
    <section className="card">
      <h2>Histórico de grupos e eventos</h2>
      <p>
        Consulte seus grupos por data de criação. Permanecem visíveis enquanto
        você participa deles.
      </p>
      <form
        className="history-filter"
        onSubmit={(event) => {
          event.preventDefault();
          setAte(new FormData(event.currentTarget).get('ate'));
          setTentativa((n) => n + 1);
        }}
      >
        <label htmlFor="historico-ate">Criados até (opcional)</label>
        <input id="historico-ate" name="ate" type="date" defaultValue="" />
        <button className="secondary" type="submit">
          Filtrar histórico
        </button>
      </form>
      {!atual && <p role="status">Carregando histórico…</p>}
      <Feedback error={atual?.erro} />
      {atual?.dados?.length === 0 && <p>Nenhum grupo neste período.</p>}
      <ul className="group-list">
        {atual?.dados?.map((g) => (
          <li key={g.id}>
            <a className="group-link" href={`#grupo/${g.id}`}>
              <h3>{g.nome}</h3>
              <p>
                Criado em{' '}
                {g.criadoEm.slice(0, 10).split('-').reverse().join('/')}
              </p>
            </a>
          </li>
        ))}
      </ul>
    </section>
  );
}
