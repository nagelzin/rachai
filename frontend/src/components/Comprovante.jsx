import { useEffect, useRef, useState } from 'react';
import { api } from '../services/api.js';
import Feedback from './Feedback.jsx';

export default function Comprovante({
  grupoId,
  despesa,
  ocupado,
  onOcupado,
  onAtualizada,
}) {
  const [arquivo, setArquivo] = useState(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [removendo, setRemovendo] = useState(false);
  const [substituindo, setSubstituindo] = useState(false);
  const cancelarRef = useRef(null);
  const removerRef = useRef(null);
  const antes = useRef(false);
  const bloqueado = ocupado || busy;
  const comprovante = despesa.comprovante;
  useEffect(() => {
    if (removendo) cancelarRef.current?.focus();
    else if (antes.current) removerRef.current?.focus();
    antes.current = removendo;
  }, [removendo]);

  async function enviar(event) {
    event.preventDefault();
    if (bloqueado) return;
    setError('');
    if (!arquivo || arquivo.size === 0 || arquivo.size > 5 * 1024 * 1024) {
      setError('Escolha um comprovante de até 5 MB, não vazio.');
      return;
    }
    if (!/\.(pdf|png|jpe?g)$/i.test(arquivo.name)) {
      setError('Escolha um arquivo JPG, PNG ou PDF.');
      return;
    }
    if (comprovante && !substituindo) {
      setSubstituindo(true);
      return;
    }
    setBusy(true);
    onOcupado(true);
    try {
      const atual = await api.enviarComprovante(
        grupoId,
        despesa.id,
        despesa.versao,
        arquivo,
      );
      onAtualizada(atual, 'Comprovante salvo.');
    } catch (err) {
      setError(err.message);
      setSubstituindo(false);
    } finally {
      setBusy(false);
      onOcupado(false);
    }
  }

  async function remover() {
    if (bloqueado) return;
    setBusy(true);
    onOcupado(true);
    setError('');
    try {
      const atual = await api.removerComprovante(
        grupoId,
        despesa.id,
        despesa.versao,
      );
      onAtualizada(atual, 'Comprovante removido. A despesa foi mantida.');
    } catch (err) {
      setError(err.message);
    } finally {
      setRemovendo(false);
      setBusy(false);
      onOcupado(false);
    }
  }

  return (
    <section
      className="receipt"
      aria-labelledby={`comprovante-${despesa.id}`}
      aria-busy={busy}
    >
      <h4 id={`comprovante-${despesa.id}`}>Comprovante</h4>
      {comprovante ? (
        <>
          <p>
            {comprovante.nome} · {Math.ceil(comprovante.tamanho / 1024)} KB
          </p>
          <div className="actions">
            {comprovante.tipo !== 'application/pdf' && (
              <a
                href={api.urlComprovante(grupoId, despesa.id)}
                target="_blank"
                rel="noreferrer"
              >
                Visualizar comprovante
              </a>
            )}
            <a href={api.urlComprovante(grupoId, despesa.id, true)} download>
              {comprovante.tipo === 'application/pdf'
                ? 'Baixar PDF'
                : 'Baixar comprovante'}
            </a>
            <button
              ref={removerRef}
              className="text-button remove"
              disabled={bloqueado || removendo || substituindo}
              onClick={() => setRemovendo(true)}
            >
              Remover comprovante
            </button>
          </div>
          {comprovante.tipo === 'application/pdf' && (
            <p>Abra o PDF baixado no seu leitor de documentos.</p>
          )}
        </>
      ) : (
        <p>Nenhum comprovante anexado.</p>
      )}
      <details>
        <summary>
          {comprovante ? 'Trocar comprovante' : 'Adicionar comprovante'}
        </summary>
        <form onSubmit={enviar}>
          <label htmlFor={`arquivo-${despesa.id}`}>
            Arquivo JPG, PNG ou PDF (até 5 MB)
          </label>
          <input
            id={`arquivo-${despesa.id}`}
            type="file"
            accept=".jpg,.jpeg,.png,.pdf"
            required
            disabled={bloqueado || removendo}
            onChange={(e) => {
              setArquivo(e.target.files?.[0] ?? null);
              setSubstituindo(false);
              setError('');
            }}
          />
          {substituindo && (
            <p role="alert">
              O comprovante atual será substituído definitivamente pelo arquivo
              selecionado.
            </p>
          )}
          <div className="actions">
            <button
              className="secondary"
              disabled={bloqueado || removendo || !arquivo}
            >
              {busy
                ? 'Aguarde…'
                : substituindo
                  ? 'Confirmar substituição'
                  : 'Enviar comprovante'}
            </button>
            {substituindo && (
              <button
                type="button"
                className="text-button"
                disabled={bloqueado}
                onClick={() => setSubstituindo(false)}
              >
                Cancelar substituição
              </button>
            )}
          </div>
        </form>
      </details>
      {removendo && (
        <div
          className="confirmation"
          role="alertdialog"
          aria-labelledby={`remover-comprovante-${despesa.id}`}
          onKeyDown={(e) => {
            if (e.key === 'Escape' && !bloqueado) setRemovendo(false);
          }}
        >
          <h4 id={`remover-comprovante-${despesa.id}`}>
            Remover o comprovante da despesa #{despesa.id}?
          </h4>
          <p>
            O arquivo será excluído definitivamente. A despesa e sua divisão
            serão mantidas.
          </p>
          <div className="actions">
            <button
              ref={cancelarRef}
              className="secondary"
              disabled={bloqueado}
              onClick={() => setRemovendo(false)}
            >
              Cancelar remoção
            </button>
            <button className="remove" disabled={bloqueado} onClick={remover}>
              Confirmar remoção
            </button>
          </div>
        </div>
      )}
      <Feedback error={error} />
    </section>
  );
}
