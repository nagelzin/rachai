import { useEffect, useRef, useState } from 'react';
import { api } from '../services/api.js';
import { formatMoney } from '../services/money.js';
import NovaDespesa from './NovaDespesa.jsx';
import Feedback from './Feedback.jsx';
import Comprovante from './Comprovante.jsx';

const ordenar = (lista) =>
  [...lista].sort((a, b) => b.data.localeCompare(a.data) || b.id - a.id);

export default function Despesas({ grupoId, usuarioId, onAlteradas }) {
  const [despesas, setDespesas] = useState([]);
  const [edicao, setEdicao] = useState(null);
  const [confirmando, setConfirmando] = useState(null);
  const [busy, setBusy] = useState(false);
  const [carregando, setCarregando] = useState(true);
  const [gravando, setGravando] = useState(false);
  const [arquivoOcupado, setArquivoOcupado] = useState(false);
  const bloqueado = busy || gravando || carregando || arquivoOcupado;
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const cancelarRef = useRef(null);
  const excluirRefs = useRef(new Map());
  const confirmacaoAnterior = useRef(null);
  const atualizarRef = useRef(null);

  useEffect(() => {
    const controller = new AbortController();
    api
      .listarDespesas(grupoId, controller.signal)
      .then(setDespesas)
      .catch((err) => {
        if (err.name !== 'AbortError') setError(err.message);
      })
      .finally(() => {
        if (!controller.signal.aborted) setCarregando(false);
      });
    return () => controller.abort();
  }, [grupoId]);

  useEffect(() => {
    if (confirmando) cancelarRef.current?.focus();
    else if (confirmacaoAnterior.current) {
      const botao = excluirRefs.current.get(confirmacaoAnterior.current.id);
      (botao ?? atualizarRef.current)?.focus();
    }
    confirmacaoAnterior.current = confirmando;
  }, [confirmando]);

  function atualizarItem(atual) {
    setDespesas((lista) =>
      ordenar([...lista.filter((d) => d.id !== atual.id), atual]),
    );
    onAlteradas();
  }

  async function atualizarLista() {
    if (bloqueado) return;
    setBusy(true);
    setError('');
    setSuccess('');
    try {
      setDespesas(await api.listarDespesas(grupoId));
      onAlteradas();
    } catch (err) {
      setError(err.message);
      if ([401, 403, 404].includes(err.status)) setDespesas([]);
    } finally {
      setBusy(false);
    }
  }

  async function consultar(id, manterEdicao = false) {
    if (bloqueado) return;
    setBusy(true);
    setError('');
    setSuccess('');
    try {
      const atual = await api.consultarDespesa(grupoId, id);
      atualizarItem(atual);
      if (manterEdicao) {
        setEdicao(atual);
        setSuccess(
          'Dados salvos recarregados. Confira e reaplique sua correção.',
        );
      }
    } catch (err) {
      setError(err.message);
      if (err.status === 404) {
        setDespesas((lista) => lista.filter((d) => d.id !== id));
        setEdicao(null);
        onAlteradas();
      }
    } finally {
      setBusy(false);
    }
  }

  async function excluir() {
    if (bloqueado || !confirmando) return;
    setBusy(true);
    setError('');
    setSuccess('');
    try {
      await api.excluirDespesa(grupoId, confirmando.id, confirmando.versao);
      setDespesas((lista) => lista.filter((d) => d.id !== confirmando.id));
      onAlteradas();
      setSuccess(`Despesa #${confirmando.id} excluída.`);
    } catch (err) {
      setError(err.message);
    } finally {
      setConfirmando(null);
      setBusy(false);
    }
  }

  return (
    <div className="expense-workspace">
      <NovaDespesa
        key={edicao ? `${edicao.id}-${edicao.versao}` : 'nova'}
        grupoId={grupoId}
        usuarioId={usuarioId}
        ocupado={busy || carregando || !!confirmando || arquivoOcupado}
        onOcupado={setGravando}
        inicial={edicao}
        onCancelar={() => setEdicao(null)}
        onRecarregar={() => consultar(edicao.id, true)}
        onSalva={(salva, editada) => {
          atualizarItem(salva);
          setEdicao(null);
          setError('');
          setSuccess(
            `Despesa #${salva.id} ${editada ? 'atualizada' : 'registrada'} com sucesso.`,
          );
        }}
      />
      <section
        className="expense-list"
        aria-labelledby="despesas-titulo"
        aria-busy={busy || carregando}
      >
        <div className="card">
          <span className="eyebrow">OS GASTOS DA TURMA</span>
          <h2 id="despesas-titulo">Despesas do grupo</h2>
          <p>Todos os lançamentos, do mais recente para o mais antigo.</p>
          <button
            ref={atualizarRef}
            className="secondary"
            disabled={bloqueado || !!edicao || !!confirmando}
            onClick={atualizarLista}
          >
            {busy ? 'Atualizando…' : 'Atualizar despesas'}
          </button>
          <Feedback error={error} success={success} />
          {carregando && <p role="status">Carregando despesas…</p>}
          {!carregando && !error && despesas.length === 0 && (
            <p className="empty-state">
              Nenhuma despesa registrada ainda. Cadastre o primeiro gasto no
              formulário acima.
            </p>
          )}
        </div>
        {despesas.map((despesa) => (
          <article
            key={despesa.id}
            className="card expense-detail"
            aria-labelledby={`despesa-${despesa.id}`}
          >
            <h3 id={`despesa-${despesa.id}`} tabIndex={-1}>
              #{despesa.id} · {despesa.descricao}
            </h3>
            <dl>
              <dt>Valor</dt>
              <dd>{formatMoney(despesa.valorTotal)}</dd>
              <dt>Data</dt>
              <dd>{despesa.data.split('-').reverse().join('/')}</dd>
              <dt>Quem pagou</dt>
              <dd>
                {despesa.pagador?.nome ?? 'Não informado no lançamento antigo'}
              </dd>
              <dt>Participantes da despesa</dt>
              <dd>
                {despesa.participantes.length
                  ? despesa.participantes.map((p) => p.nome).join(', ')
                  : 'Não informados no lançamento antigo'}
              </dd>
            </dl>
            {despesa.participantes.length > 0 && (
              <div className="division-preview">
                <h4>Divisão da despesa</h4>
                <ul className="division-list">
                  {despesa.participantes.map((p) => (
                    <li key={p.id}>
                      <span>{p.nome}</span>
                      <strong>
                        {p.valorParte == null
                          ? 'Não calculada'
                          : formatMoney(p.valorParte)}
                      </strong>
                    </li>
                  ))}
                </ul>
              </div>
            )}
            <div className="actions">
              <button
                className="secondary"
                disabled={bloqueado || !!edicao || !!confirmando}
                onClick={() => {
                  setEdicao(despesa);
                  setError('');
                  setSuccess('');
                }}
              >
                Editar despesa
              </button>
              <button
                ref={(node) => {
                  if (node) excluirRefs.current.set(despesa.id, node);
                  else excluirRefs.current.delete(despesa.id);
                }}
                className="text-button remove"
                disabled={bloqueado || !!edicao || !!confirmando}
                onClick={() => setConfirmando(despesa)}
              >
                Excluir despesa
              </button>
              <button
                className="text-button"
                disabled={bloqueado || !!edicao || !!confirmando}
                onClick={() => consultar(despesa.id)}
              >
                Recarregar despesa
              </button>
            </div>
            {confirmando?.id === despesa.id && (
              <div
                className="confirm-panel"
                role="alertdialog"
                aria-labelledby={`excluir-titulo-${despesa.id}`}
                aria-describedby={`excluir-aviso-${despesa.id}`}
                onKeyDown={(e) => {
                  if (e.key === 'Escape' && !busy) setConfirmando(null);
                }}
              >
                <h3 id={`excluir-titulo-${despesa.id}`}>
                  Excluir a despesa #{despesa.id}?
                </h3>
                <p id={`excluir-aviso-${despesa.id}`}>
                  {despesa.descricao} · {formatMoney(despesa.valorTotal)}. A
                  exclusão é definitiva.
                </p>
                <div className="actions">
                  <button
                    ref={cancelarRef}
                    className="secondary"
                    disabled={bloqueado}
                    onClick={() => setConfirmando(null)}
                  >
                    Cancelar exclusão
                  </button>
                  <button
                    className="remove"
                    disabled={bloqueado}
                    onClick={excluir}
                  >
                    Confirmar exclusão
                  </button>
                </div>
              </div>
            )}
            <Comprovante
              key={`${despesa.id}-${despesa.versao}`}
              grupoId={grupoId}
              despesa={despesa}
              ocupado={bloqueado || !!edicao || !!confirmando}
              onOcupado={setArquivoOcupado}
              onAtualizada={(atual, mensagem) => {
                atualizarItem(atual);
                setError('');
                setSuccess(mensagem);
              }}
            />
          </article>
        ))}
      </section>
    </div>
  );
}
