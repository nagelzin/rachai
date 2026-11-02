import { useEffect, useState } from 'react';
import { api } from '../services/api.js';
import { formatMoney } from '../services/money.js';
import Feedback from './Feedback.jsx';

export default function DetalheSaldo({ grupoId, participanteId }) {
  const [resultado, setResultado] = useState(null);
  useEffect(() => {
    const controller = new AbortController();
    api
      .detalharSaldo(grupoId, participanteId, controller.signal)
      .then((dados) => {
        if (!controller.signal.aborted) setResultado({ dados });
      })
      .catch((erro) => {
        if (!controller.signal.aborted) setResultado({ erro: erro.message });
      });
    return () => controller.abort();
  }, [grupoId, participanteId]);
  return (
    <div className="balance-detail">
      {!resultado && <p role="status">Carregando detalhamento…</p>}
      <Feedback error={resultado?.erro} />
      {resultado?.dados && !resultado.dados.completo && (
        <p role="status">
          Corrija as despesas incompletas antes de consultar este saldo.
        </p>
      )}
      {resultado?.dados?.completo && (
        <>
          <p>
            Pago menos sua parte em cada despesa. Valores negativos reduzem o
            saldo.
          </p>
          {!resultado.dados.despesas.length && (
            <p>Nenhuma despesa para esta pessoa.</p>
          )}
          <ul className="balance-list">
            {resultado.dados.despesas.map((d) => (
              <li key={d.despesaId}>
                <h4>
                  #{d.despesaId} · {d.descricao}
                </h4>
                <p>
                  {d.data.split('-').reverse().join('/')} · Pago por{' '}
                  {d.pagadorNome}
                </p>
                <dl>
                  <div>
                    <dt>Total da despesa</dt>
                    <dd>{formatMoney(d.totalDespesa)}</dd>
                  </div>
                  <div>
                    <dt>Esta pessoa pagou</dt>
                    <dd>{formatMoney(d.pago)}</dd>
                  </div>
                  <div>
                    <dt>Sua parte</dt>
                    <dd>{formatMoney(d.parte)}</dd>
                  </div>
                  <div>
                    <dt>Efeito no saldo</dt>
                    <dd>
                      {d.impacto.startsWith('-') ? '− ' : '+ '}
                      {formatMoney(d.impacto.replace(/^-/, ''))}
                    </dd>
                  </div>
                </dl>
              </li>
            ))}
          </ul>
        </>
      )}
    </div>
  );
}
