package br.com.rachai.service;

import br.com.rachai.dto.*;
import br.com.rachai.exception.*;
import br.com.rachai.repository.DespesaParticipanteRepository;
import br.com.rachai.repository.DespesaRepository;
import br.com.rachai.repository.GrupoParticipanteRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ParticipanteService {
    private final GrupoService grupos;
    private final GrupoParticipanteRepository participantes;
    private final DespesaRepository despesas;
    private final DespesaParticipanteRepository envolvidos;
    private final br.com.rachai.repository.AcertoRepository acertos;

    public ParticipanteService(
            GrupoService grupos,
            GrupoParticipanteRepository participantes,
            DespesaRepository despesas,
            DespesaParticipanteRepository envolvidos,
            br.com.rachai.repository.AcertoRepository acertos) {
        this.grupos = grupos;
        this.participantes = participantes;
        this.despesas = despesas;
        this.envolvidos = envolvidos;
        this.acertos = acertos;
    }

    public List<ParticipanteResponse> listar(Long grupoId) {
        grupos.buscar(grupoId);
        return participantes.findByGrupoIdOrderByIdAsc(grupoId).stream()
                .map(ParticipanteResponse::from)
                .toList();
    }

    @Transactional
    public void remover(Long grupoId, Long participanteId) {
        var grupo = grupos.administrador(grupoId);
        grupos.bloquear(grupoId);
        var p =
                participantes
                        .findByIdAndGrupoId(participanteId, grupoId)
                        .orElseThrow(
                                () ->
                                        new NaoEncontradoException(
                                                "Participante não encontrado neste grupo."));
        if (p.getUsuario() != null && grupo.getResponsavel().getId().equals(p.getUsuario().getId()))
            throw new ConflitoException("O administrador não pode ser removido do grupo.");
        if (despesas.existsByPagadorId(participanteId)
                || envolvidos.existsByParticipanteId(participanteId))
            throw new ConflitoException(
                    "Essa pessoa está vinculada a uma despesa e não pode ser removida. Corrija o"
                            + " lançamento antes de remover o membro.");
        if (acertos.existsByDevedorIdOrRecebedorId(participanteId, participanteId))
            throw new ConflitoException(
                    "Essa pessoa possui histórico de acertos e deve permanecer no grupo para"
                        + " preservar os registros.");
        participantes.delete(p);
        participantes.flush();
    }
}
