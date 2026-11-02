import { useEffect, useState } from 'react';
import { api } from '../services/api.js';
import { formatMoney } from '../services/money.js';
import Feedback from './Feedback.jsx';

export default function Acompanhamento({ grupoId, revisao }) {
  const [tentativa, setTentativa] = useState(0);
  const [resultado, setResultado] = useState(null);
  const chave = `${grupoId}-${revisao}-${tentativa}`;
  const atual = resultado?.chave === chave ? resultado : null;
  useEffect(() => {
    const controller = new AbortController();
    api
      .consultarProgresso(grupoId, controller.signal)
      .then((dados) => {
        if (!controller.signal.aborted) setResultado({ chave, dados });
      })
      .catch((erro) => {
        if (!controller.signal.aborted)
          setResultado({ chave, erro: erro.message });
      });
    return () => controller.abort();
  }, [grupoId, chave]);
  const d = atual?.dados;
  return (
    <section
      className="card"
      aria-labelledby="progresso-titulo"
      aria-busy={!atual}
    >
      <span className="eyebrow">ACOMPANHE A TURMA</span>
      <h2 id="progresso-titulo">Pendências e progresso</h2>
      <button
        className="secondary"
        disabled={!atual}
        onClick={() => setTentativa((n) => n + 1)}
      >
        Atualizar acompanhamento
      </button>
      {!atual && <p role="status">Consultando pagamentos…</p>}
      <Feedback error={atual?.erro} />
      {d && !d.completo && (
        <p>
          Complete as despesas indicadas nos saldos para consultar o progresso.
          Os indicadores ainda não estão disponíveis.
        </p>
      )}
      {d?.completo && (
        <>
          {d.semAcertos ? (
            <p>Nenhum acerto necessário com os dados atuais.</p>
          ) : (
            <>
              <p>
                <strong>{d.percentualConcluido.replace('.', ',')}%</strong> dos
                acertos concluídos
              </p>
              <progress
                aria-label="Percentual de acertos concluídos"
                max="100"
                value={Number(d.percentualConcluido)}
              />
              <p>
                {d.quitados} quitado(s) · {d.pendentes} pendente(s)
              </p>
            </>
          )}
          <dl>
            <div>
              <dt>Pagamentos registrados</dt>
              <dd>{formatMoney(d.valorPago)}</dd>
            </div>
            <div>
              <dt>Falta acertar</dt>
              <dd>{formatMoney(d.valorPendente)}</dd>
            </div>
          </dl>
          <p className="balance-note">
            Progresso por quantidade de acertos, considerando registros válidos
            e sugestões atuais. Novas despesas ou correções podem mudar esse
            percentual.
          </p>
          <h3>Pessoas com valores pendentes</h3>
          {!d.participantes.length && <p>Ninguém tem valores pendentes.</p>}
          <ul className="balance-list">
            {d.participantes.map((p) => (
              <li key={p.participanteId}>
                <h4>
                  {p.nome} (#{p.participanteId})
                </h4>
                {p.aPagar !== '0.00' && (
                  <p>
                    A pagar: <strong>{formatMoney(p.aPagar)}</strong>
                  </p>
                )}
                {p.aReceber !== '0.00' && (
                  <p>
                    A receber: <strong>{formatMoney(p.aReceber)}</strong>
                  </p>
                )}
              </li>
            ))}
          </ul>
        </>
      )}
    </section>
  );
}
