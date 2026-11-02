import Participantes from '../components/Participantes.jsx';
import Despesas from '../components/Despesas.jsx';
import Saldos from '../components/Saldos.jsx';
import { useCallback, useState } from 'react';
export default function Grupo({ grupo, usuario }) {
  const administrador = grupo.usuarioResponsavelId === usuario.id;
  const [revisao, setRevisao] = useState(0);
  const atualizarSaldos = useCallback(() => setRevisao((n) => n + 1), []);
  return (
    <>
      <section className="group-heading">
        <span className="eyebrow">
          {administrador
            ? 'VOCÊ É O ADMINISTRADOR'
            : `ADMINISTRADOR: ${grupo.responsavelNome}`}
        </span>
        <h1>{grupo.nome}</h1>
        {grupo.descricao && <p>{grupo.descricao}</p>}
        <p className="group-hint">
          Apenas os membros deste grupo têm acesso ao seu conteúdo.
        </p>
      </section>
      <div className="group-sections">
        <div className="group-summary">
          <Participantes
            grupoId={grupo.id}
            administrador={administrador}
            onAlterados={atualizarSaldos}
          />
          <Saldos grupoId={grupo.id} usuarioId={usuario.id} revisao={revisao} />
        </div>
        <Despesas
          key={grupo.id}
          grupoId={grupo.id}
          usuarioId={usuario.id}
          onAlteradas={atualizarSaldos}
        />
      </div>
    </>
  );
}
