import { useEffect, useState } from 'react';
import { api } from '../services/api.js';
import { formatMoney } from '../services/money.js';
import Feedback from './Feedback.jsx';
import DetalheSaldo from './DetalheSaldo.jsx';

const situacoes = {
  RECEBER: 'A receber',
  PAGAR: 'A pagar',
  ZERADO: 'Saldo zerado',
};

export default function Saldos({ grupoId, usuarioId, revisao }) {
  const [tentativa, setTentativa] = useState(0);
  const [resposta, setResposta] = useState(null);
  const [detalhe, setDetalhe] = useState(null);
  const chave = `${grupoId}-${revisao}-${tentativa}`;
  const atual = resposta?.chave === chave ? resposta : null;
  const carregando = atual === null;
  useEffect(() => {
    const controller = new AbortController();
    api
      .consultarSaldos(grupoId, controller.signal)
      .then((dados) => {
        if (!controller.signal.aborted) setResposta({ chave, dados });
      })
      .catch((erro) => {
        if (!controller.signal.aborted)
          setResposta({ chave, erro: erro.message });
      });
    return () => controller.abort();
  }, [grupoId, chave]);
  return (
    <section
      className="card balances"
      aria-labelledby="saldos-titulo"
      aria-busy={carregando}
    >
      <span className="eyebrow">AS CONTAS DA TURMA</span>
      <h2 id="saldos-titulo">Saldos do grupo</h2>
      <p>O que cada pessoa pagou menos sua parte nas despesas.</p>
      <button
        className="secondary"
        disabled={carregando}
        onClick={() => setTentativa((n) => n + 1)}
      >
        {carregando ? 'Atualizando saldos…' : 'Atualizar saldos'}
      </button>
      <Feedback error={atual?.erro} />
      {carregando && <p role="status">Calculando saldos…</p>}
      {atual?.dados && !atual.dados.completo && (
        <div className="balance-warning" role="status">
          <h3>Complete as despesas para calcular os saldos</h3>
          <p>
            Há lançamentos sem pagador ou com divisão incompleta. Corrija as
            despesas abaixo; os saldos serão calculados após salvar.
          </p>
          <ul>
            {atual.dados.despesasPendentes.map((id) => (
              <li key={id}>
                <button
                  className="text-button"
                  onClick={() => {
                    const titulo = document.getElementById(`despesa-${id}`);
                    titulo?.scrollIntoView({ block: 'center' });
                    titulo?.focus({ preventScroll: true });
                  }}
                >
                  Revisar despesa #{id}
                </button>
              </li>
            ))}
          </ul>
        </div>
      )}
      {atual?.dados?.completo && (
        <>
          <ul className="balance-list">
            {atual.dados.participantes.map((p) => (
              <li
                key={p.participanteId}
                className={`balance-item balance-${p.situacao.toLowerCase()}`}
              >
                <h3>
                  {p.nome}
                  {p.usuarioId === usuarioId && <small>Você</small>}
                </h3>
                <p className="balance-result">
                  <span>{situacoes[p.situacao]}</span>
                  <strong>{formatMoney(p.saldo.replace(/^-/, ''))}</strong>
                </p>
                <dl>
                  <div>
                    <dt>Pagou nas despesas</dt>
                    <dd>{formatMoney(p.totalPago)}</dd>
                  </div>
                  <div>
                    <dt>Sua parte nas despesas</dt>
                    <dd>{formatMoney(p.totalDevido)}</dd>
                  </div>
                </dl>
                <button
                  className="text-button"
                  aria-expanded={detalhe === p.participanteId}
                  onClick={() =>
                    setDetalhe(
                      detalhe === p.participanteId ? null : p.participanteId,
                    )
                  }
                >
                  {detalhe === p.participanteId
                    ? 'Fechar detalhamento'
                    : 'Ver despesas deste saldo'}
                </button>
                {detalhe === p.participanteId && (
                  <DetalheSaldo
                    key={`${chave}-${p.participanteId}`}
                    grupoId={grupoId}
                    participanteId={p.participanteId}
                  />
                )}
              </li>
            ))}
          </ul>
          <p className="balance-note">
            Estes saldos explicam as despesas. Consulte Acertos do grupo para
            ver o que ainda falta após os pagamentos registrados.
          </p>
        </>
      )}
    </section>
  );
}
