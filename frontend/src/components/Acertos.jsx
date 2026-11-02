import { useEffect, useState } from 'react';
import { api } from '../services/api.js';
import { formatMoney } from '../services/money.js';
import Feedback from './Feedback.jsx';

export default function Acertos({ grupoId, revisao, usuarioId, onAlterados }) {
  const [tentativa, setTentativa] = useState(0);
  const [resultado, setResultado] = useState(null);
  const [mensagem, setMensagem] = useState('');
  const [mostrarResumo, setMostrarResumo] = useState(false);
  const [ocupado, setOcupado] = useState(false);
  const [erroAcao, setErroAcao] = useState('');
  const chave = `${grupoId}-${revisao}-${tentativa}`;
  const atual = resultado?.chave === chave ? resultado : null;
  useEffect(() => {
    const controller = new AbortController();
    api
      .consultarAcertos(grupoId, controller.signal)
      .then((dados) => {
        if (!controller.signal.aborted) setResultado({ chave, dados });
      })
      .catch((erro) => {
        if (!controller.signal.aborted)
          setResultado({ chave, erro: erro.message });
      });
    return () => controller.abort();
  }, [grupoId, chave]);
  async function compartilhar(copiar) {
    setMensagem('');
    try {
      if (copiar) {
        await navigator.clipboard.writeText(atual.dados.resumo);
        setMensagem('Resumo copiado.');
      } else
        await navigator.share({
          title: 'Resumo Rachaí',
          text: atual.dados.resumo,
        });
    } catch (erro) {
      if (erro.name !== 'AbortError') {
        setMostrarResumo(true);
        setMensagem('Selecione e copie o texto do resumo abaixo.');
      }
    }
  }
  async function registrar(a) {
    if (
      ocupado ||
      !window.confirm(
        `Registrar que ${a.devedorNome} já pagou ${formatMoney(a.valor)} a ${a.recebedorNome}? Confirme somente se o pagamento realmente ocorreu.`,
      )
    )
      return;
    setOcupado(true);
    setErroAcao('');
    setMensagem('');
    try {
      await api.registrarAcerto(grupoId, {
        devedorId: a.devedorId,
        recebedorId: a.recebedorId,
        valor: a.valor,
        revisao: atual.dados.revisao,
      });
      setMensagem('Pagamento registrado.');
      onAlterados();
    } catch (e) {
      setErroAcao(e.message);
      setTentativa((n) => n + 1);
    } finally {
      setOcupado(false);
    }
  }
  async function cancelar(a) {
    if (
      ocupado ||
      !window.confirm(
        'Cancelar este registro de pagamento? O histórico será preservado e as pendências serão recalculadas.',
      )
    )
      return;
    setOcupado(true);
    setErroAcao('');
    setMensagem('');
    try {
      await api.cancelarAcerto(grupoId, a.id, a.versao);
      setMensagem('Registro cancelado.');
      onAlterados();
    } catch (e) {
      setErroAcao(e.message);
      setTentativa((n) => n + 1);
    } finally {
      setOcupado(false);
    }
  }
  const podeRegistrar = (a) =>
    a.devedorUsuarioId === usuarioId || a.recebedorUsuarioId === usuarioId;
  return (
    <section
      className="card settlements"
      aria-labelledby="acertos-titulo"
      aria-busy={!atual}
    >
      <span className="eyebrow">PARA ACERTAR AS CONTAS</span>
      <h2 id="acertos-titulo">Acertos do grupo</h2>
      <p>
        Sugestões para equilibrar os saldos. Nenhuma transferência é feita pelo
        Rachaí.
      </p>
      <button
        className="secondary"
        disabled={!atual || ocupado}
        onClick={() => {
          setMensagem('');
          setTentativa((n) => n + 1);
        }}
      >
        Atualizar acertos
      </button>
      {!atual && <p role="status">Calculando acertos…</p>}
      <Feedback error={erroAcao || atual?.erro} success={mensagem} />
      {atual?.dados && !atual.dados.completo && (
        <p role="status">
          Corrija as despesas incompletas indicadas nos saldos antes de gerar
          acertos ou compartilhar o resumo.
        </p>
      )}
      {atual?.dados?.completo && (
        <>
          {!atual.dados.sugestoes.length && (
            <p>Nenhum acerto necessário com os dados atuais.</p>
          )}
          <ul className="balance-list">
            {atual.dados.sugestoes.map((a) => (
              <li key={`${a.devedorId}-${a.recebedorId}`}>
                <p>
                  <strong>
                    {a.devedorNome} (#{a.devedorId})
                  </strong>{' '}
                  paga a{' '}
                  <strong>
                    {a.recebedorNome} (#{a.recebedorId})
                  </strong>
                </p>
                <p className="balance-result">{formatMoney(a.valor)}</p>
                {podeRegistrar(a) && (
                  <button
                    className="secondary"
                    disabled={ocupado}
                    onClick={() => registrar(a)}
                  >
                    {a.devedorUsuarioId === usuarioId
                      ? 'Registrar que paguei'
                      : 'Confirmar que recebi'}
                  </button>
                )}
              </li>
            ))}
          </ul>
          <div className="actions">
            <button className="secondary" onClick={() => compartilhar(true)}>
              Copiar resumo
            </button>
            {typeof navigator.share === 'function' && (
              <button className="secondary" onClick={() => compartilhar(false)}>
                Compartilhar resumo
              </button>
            )}
            <button
              className="text-button"
              aria-expanded={mostrarResumo}
              onClick={() => setMostrarResumo(!mostrarResumo)}
            >
              {mostrarResumo ? 'Fechar resumo' : 'Ver resumo'}
            </button>
          </div>
          {mostrarResumo && (
            <>
              <label htmlFor="resumo-grupo">Resumo para compartilhar</label>
              <textarea
                id="resumo-grupo"
                rows={12}
                readOnly
                value={atual.dados.resumo}
              />
            </>
          )}
        </>
      )}
      {!!atual?.dados?.registros?.length && (
        <section aria-label="Histórico de pagamentos">
          <h3>Pagamentos registrados</h3>
          <p>
            Registros informados por quem pagou ou recebeu, sem verificação
            bancária.
          </p>
          <ul className="balance-list">
            {[...atual.dados.registros].reverse().map((a) => (
              <li key={a.id}>
                <p>
                  {a.devedorNome} (#{a.devedorId}) → {a.recebedorNome} (#
                  {a.recebedorId}) · <strong>{formatMoney(a.valor)}</strong>
                </p>
                <p>
                  {a.status === 'PAGO' ? 'Pago' : 'Registro cancelado'} ·{' '}
                  {a.criadoEm.slice(0, 10).split('-').reverse().join('/')}
                </p>
                {a.status === 'PAGO' && podeRegistrar(a) && (
                  <button
                    className="text-button"
                    disabled={ocupado}
                    onClick={() => cancelar(a)}
                  >
                    Cancelar registro
                  </button>
                )}
              </li>
            ))}
          </ul>
        </section>
      )}
    </section>
  );
}
