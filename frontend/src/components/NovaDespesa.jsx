import { useEffect, useRef, useState } from 'react';
import { api } from '../services/api.js';
import { parseMoney, formatMoney, todayLocal } from '../services/money.js';
import Feedback from './Feedback.jsx';

export default function NovaDespesa({
  grupoId,
  inicial,
  onSalva,
  onCancelar,
  onRecarregar,
  usuarioId,
  ocupado,
  onOcupado,
}) {
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [busy, setBusy] = useState(false);
  const bloqueado = busy || ocupado;
  const [participantes, setParticipantes] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [recarregar, setRecarregar] = useState(0);
  const [selecionados, setSelecionados] = useState(
    inicial?.participantes.map((p) => p.id) ?? [],
  );
  const [pagador, setPagador] = useState(String(inicial?.pagador?.id ?? ''));
  const [conflito, setConflito] = useState(false);
  const [previa, setPrevia] = useState(null);
  const [personalizada, setPersonalizada] = useState(
    inicial?.tipoDivisao === 'PERSONALIZADA',
  );
  const descricaoRef = useRef(null);
  useEffect(() => {
    if (inicial) descricaoRef.current?.focus();
  }, [inicial]);
  const nomeOpcao = (p) =>
    `${p.nome}${p.usuarioId === usuarioId ? ' (você)' : ''}${participantes.filter((v) => v.nome === p.nome).length > 1 ? ` · participante ${p.id}` : ''}`;
  useEffect(() => {
    const controller = new AbortController();
    api
      .listarParticipantes(grupoId, controller.signal)
      .then((lista) => {
        const ativos = lista.filter((p) => p.usuarioId != null);
        setParticipantes(ativos);
        setSelecionados((ids) =>
          ids.filter((id) => ativos.some((p) => p.id === id)),
        );
        setPagador((id) => (ativos.some((p) => String(p.id) === id) ? id : ''));
      })
      .catch((err) => {
        if (err.name !== 'AbortError') setError(err.message);
      })
      .finally(() => {
        if (!controller.signal.aborted) setCarregando(false);
      });
    return () => controller.abort();
  }, [grupoId, recarregar]);

  async function salvar(event) {
    event.preventDefault();
    if (bloqueado) return;
    const form = event.currentTarget;
    const fields = new FormData(form);
    setError('');
    setSuccess('');
    setConflito(false);
    let valorTotal;
    try {
      valorTotal = parseMoney(fields.get('valor'));
    } catch (err) {
      setError(err.message);
      return;
    }
    const descricao = fields.get('descricao').trim();
    if (!descricao) {
      setError('Informe a descrição da despesa.');
      return;
    }
    if (!selecionados.length) {
      setError('Selecione pelo menos um participante da despesa.');
      return;
    }
    setBusy(true);
    onOcupado(true);
    try {
      const dados = {
        descricao,
        valorTotal,
        data: fields.get('data'),
        pagadorId: Number(pagador),
        participanteIds: selecionados,
        valoresIndividuais: personalizada
          ? Object.fromEntries(
              selecionados.map((id) => [
                id,
                parseMoney(fields.get(`parcela-${id}`) ?? '', true),
              ]),
            )
          : undefined,
      };
      const chave = JSON.stringify(dados);
      if (previa?.chave !== chave) {
        const parcelas = await api.previaDivisao(
          grupoId,
          valorTotal,
          selecionados,
          dados.valoresIndividuais,
        );
        setPrevia({ chave, parcelas });
        return;
      }
      const despesa = inicial
        ? await api.editarDespesa(grupoId, inicial.id, inicial.versao, dados)
        : await api.cadastrarDespesa(grupoId, dados);
      setSuccess(
        `Despesa #${despesa.id} registrada: ${despesa.descricao}, ${formatMoney(despesa.valorTotal)}. Pago por ${despesa.pagador.nome}.`,
      );
      form.reset();
      setSelecionados([]);
      setPagador('');
      setPrevia(null);
      setPersonalizada(false);
      onSalva(despesa, !!inicial);
    } catch (err) {
      setPrevia(null);
      setError(err.message);
      setConflito(err.status === 409);
    } finally {
      setBusy(false);
      onOcupado(false);
    }
  }

  return (
    <section className="card expense-card" aria-labelledby="despesa-titulo">
      <span className="eyebrow">CADA GASTO NO SEU LUGAR</span>
      <h2 id="despesa-titulo">
        {inicial ? `Editar despesa #${inicial.id}` : 'Registrar uma despesa'}
      </h2>
      <p>
        {inicial
          ? 'Confira os dados e salve a correção.'
          : 'Anote agora para não esquecer depois.'}
      </p>
      {inicial && (!inicial.pagador || !inicial.participantes.length) && (
        <p className="feedback">
          Este lançamento antigo precisa de pagador e participantes. Preencha
          esses campos para salvar.
        </p>
      )}
      <form onSubmit={salvar} onChange={() => setPrevia(null)} aria-busy={busy}>
        <label htmlFor="despesa-descricao">O que foi pago?</label>
        <input
          id="despesa-descricao"
          ref={descricaoRef}
          name="descricao"
          placeholder="Ex.: Compras do mercado"
          required
          maxLength={200}
          disabled={bloqueado}
          defaultValue={inicial?.descricao ?? ''}
        />
        <div className="field-pair">
          <div>
            <label htmlFor="despesa-valor">Valor (R$)</label>
            <input
              id="despesa-valor"
              name="valor"
              inputMode="decimal"
              placeholder="0,00"
              required
              maxLength={13}
              aria-describedby="valor-ajuda"
              disabled={bloqueado}
              defaultValue={inicial?.valorTotal.replace('.', ',') ?? ''}
            />
            <small id="valor-ajuda">Ex.: 45,90, sem pontos de milhar.</small>
          </div>
          <div>
            <label htmlFor="despesa-data">Data</label>
            <input
              id="despesa-data"
              name="data"
              type="date"
              required
              defaultValue={inicial?.data ?? todayLocal()}
              disabled={bloqueado}
            />
          </div>
        </div>
        <label htmlFor="despesa-pagador">Quem pagou?</label>
        <select
          id="despesa-pagador"
          name="pagador"
          required
          disabled={bloqueado || carregando}
          value={pagador}
          onChange={(e) => setPagador(e.target.value)}
        >
          <option value="">
            {carregando ? 'Carregando participantes…' : 'Selecione o pagador'}
          </option>
          {participantes.map((p) => (
            <option key={p.id} value={p.id}>
              {nomeOpcao(p)}
            </option>
          ))}
        </select>
        <button
          type="button"
          className="text-button"
          disabled={bloqueado || carregando}
          onClick={() => {
            setCarregando(true);
            setPrevia(null);
            setError('');
            setRecarregar((n) => n + 1);
          }}
        >
          Atualizar opções de participantes
        </button>
        <fieldset
          className="expense-members"
          disabled={bloqueado || carregando}
        >
          <legend>Quem participa desta despesa?</legend>
          <p>
            Marque somente quem deve participar do gasto. O pagador pode ficar
            fora.
          </p>
          <button
            type="button"
            className="text-button"
            onClick={() => {
              setPrevia(null);
              setSelecionados(participantes.map((p) => p.id));
            }}
          >
            Selecionar todos
          </button>
          {participantes.map((p) => (
            <label key={p.id} className="check-option">
              <input
                type="checkbox"
                checked={selecionados.includes(p.id)}
                onChange={(e) =>
                  setSelecionados((ids) =>
                    e.target.checked
                      ? [...ids, p.id]
                      : ids.filter((id) => id !== p.id),
                  )
                }
              />
              {nomeOpcao(p)}
            </label>
          ))}
          <small>{selecionados.length} participante(s) selecionado(s).</small>
        </fieldset>
        <label htmlFor="tipo-divisao">Como dividir?</label>
        <select
          id="tipo-divisao"
          disabled={bloqueado}
          value={personalizada ? 'PERSONALIZADA' : 'IGUAL'}
          onChange={(e) => {
            setPersonalizada(e.target.value === 'PERSONALIZADA');
            setPrevia(null);
          }}
        >
          <option value="IGUAL">Igualmente entre os selecionados</option>
          <option value="PERSONALIZADA">Informar valores individuais</option>
        </select>
        {personalizada && (
          <fieldset disabled={bloqueado}>
            <legend>Parte de cada pessoa (R$)</legend>
            <p>
              Use zero quando necessário. A soma deve ser igual ao total da
              despesa.
            </p>
            {participantes
              .filter((p) => selecionados.includes(p.id))
              .map((p) => (
                <div key={p.id}>
                  <label htmlFor={`parcela-${p.id}`}>{nomeOpcao(p)}</label>
                  <input
                    id={`parcela-${p.id}`}
                    name={`parcela-${p.id}`}
                    inputMode="decimal"
                    maxLength={13}
                    required
                    defaultValue={
                      inicial?.participantes
                        .find((v) => v.id === p.id)
                        ?.valorParte?.replace('.', ',') ?? ''
                    }
                  />
                </div>
              ))}
          </fieldset>
        )}
        <Feedback error={error} success={success} />
        {previa && (
          <section
            className="division-preview"
            aria-label="Prévia da divisão"
            aria-live="polite"
          >
            <h3>Confira quanto cabe a cada pessoa</h3>
            <ul className="division-list">
              {previa.parcelas.map((p) => (
                <li key={p.id}>
                  <span>{nomeOpcao(p)}</span>
                  <strong>{formatMoney(p.valorParte)}</strong>
                </li>
              ))}
            </ul>
            <p>
              {personalizada
                ? 'Valores individuais conferidos. A soma corresponde ao total da despesa.'
                : 'Centavos restantes são distribuídos pela ordem de entrada no grupo. O total das parcelas corresponde ao valor da despesa.'}
            </p>
          </section>
        )}
        {conflito && inicial && (
          <button
            type="button"
            className="secondary"
            disabled={bloqueado}
            onClick={onRecarregar}
          >
            Recarregar dados salvos
          </button>
        )}
        <button
          className="primary"
          disabled={bloqueado || carregando || !participantes.length}
        >
          {busy
            ? 'Aguarde…'
            : !previa
              ? 'Conferir divisão'
              : inicial
                ? 'Confirmar alterações'
                : 'Confirmar despesa'}
        </button>
        {inicial && (
          <button
            type="button"
            className="text-button"
            disabled={bloqueado}
            onClick={onCancelar}
          >
            Cancelar edição
          </button>
        )}
      </form>
    </section>
  );
}
