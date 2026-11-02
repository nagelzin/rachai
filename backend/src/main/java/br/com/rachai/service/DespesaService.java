package br.com.rachai.service;

import br.com.rachai.dto.*;
import br.com.rachai.exception.ConflitoException;
import br.com.rachai.exception.NaoEncontradoException;
import br.com.rachai.model.Despesa;
import br.com.rachai.model.GrupoParticipante;
import br.com.rachai.repository.DespesaRepository;
import br.com.rachai.repository.GrupoParticipanteRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class DespesaService {
    private final GrupoService grupos;
    private final DespesaRepository despesas;
    private final GrupoParticipanteRepository participantes;

    public DespesaService(
            GrupoService grupos,
            DespesaRepository despesas,
            GrupoParticipanteRepository participantes) {
        this.grupos = grupos;
        this.despesas = despesas;
        this.participantes = participantes;
    }

    @Transactional
    public DespesaResponse cadastrar(Long grupoId, CriarDespesaRequest request) {
        grupos.buscar(grupoId);
        grupos.bloquear(grupoId);
        // Revalida após o lock: o membro pode ter sido removido enquanto aguardava.
        var grupo = grupos.buscar(grupoId);
        var pagador =
                participantes
                        .findByIdAndGrupoId(request.pagadorId(), grupoId)
                        .filter(p -> p.getUsuario() != null)
                        .orElseThrow(
                                () ->
                                        new ConflitoException(
                                                "Selecione um pagador que participe deste grupo."
                                                        + " Atualize a lista."));
        var despesa =
                new Despesa(
                        grupo, request.descricao().strip(), request.valorTotal(), request.data());
        despesa.definirPagador(pagador);
        despesa.definirEnvolvidos(validarEnvolvidos(grupoId, request.participanteIds()));
        personalizar(despesa, request.valoresIndividuais());
        return DespesaResponse.from(despesas.save(despesa));
    }

    private List<GrupoParticipante> validarEnvolvidos(Long grupoId, List<Long> ids) {
        if (new HashSet<>(ids).size() != ids.size())
            throw new ConflitoException("Selecione cada participante apenas uma vez.");
        return ids.stream()
                .map(
                        id ->
                                participantes
                                        .findByIdAndGrupoId(id, grupoId)
                                        .filter(p -> p.getUsuario() != null)
                                        .orElseThrow(
                                                () ->
                                                        new ConflitoException(
                                                                "Todos os envolvidos devem"
                                                                        + " participar deste grupo."
                                                                        + " Atualize a lista.")))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ParticipacaoDespesaResponse> previa(Long grupoId, PreviaDivisaoRequest request) {
        grupos.buscar(grupoId);
        var membros = validarEnvolvidos(grupoId, request.participanteIds());
        java.util.Map<Long, java.math.BigDecimal> parcelas;
        try {
            parcelas =
                    br.com.rachai.model.DivisaoPersonalizada.calcular(
                            request.valorTotal(),
                            request.participanteIds(),
                            request.valoresIndividuais());
        } catch (IllegalArgumentException e) {
            throw new ConflitoException(e.getMessage());
        }
        return membros.stream()
                .sorted(Comparator.comparing(GrupoParticipante::getId))
                .map(p -> ParticipacaoDespesaResponse.from(p, parcelas.get(p.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DespesaResponse> listar(Long grupoId) {
        grupos.buscar(grupoId);
        return despesas.findByGrupoIdOrderByDataDescIdDesc(grupoId).stream()
                .map(DespesaResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DespesaResponse consultar(Long grupoId, Long id) {
        grupos.buscar(grupoId);
        return DespesaResponse.from(buscar(grupoId, id));
    }

    @Transactional
    public DespesaResponse editar(Long grupoId, Long id, EditarDespesaRequest request) {
        bloquearComoMembro(grupoId);
        var despesa = buscar(grupoId, id);
        conferirVersao(despesa, request.versao());
        var dados = request.dados();
        var pagador =
                participantes
                        .findByIdAndGrupoId(dados.pagadorId(), grupoId)
                        .filter(p -> p.getUsuario() != null)
                        .orElseThrow(
                                () ->
                                        new ConflitoException(
                                                "Selecione um pagador que participe deste grupo."
                                                        + " Atualize a lista."));
        var envolvidos = validarEnvolvidos(grupoId, dados.participanteIds());
        despesa.corrigir(
                dados.descricao().strip(), dados.valorTotal(), dados.data(), pagador, envolvidos);
        personalizar(despesa, dados.valoresIndividuais());
        return DespesaResponse.from(despesas.saveAndFlush(despesa));
    }

    @Transactional
    public void excluir(Long grupoId, Long id, long versao) {
        bloquearComoMembro(grupoId);
        var despesa = buscar(grupoId, id);
        conferirVersao(despesa, versao);
        despesas.delete(despesa);
        despesas.flush();
    }

    private Despesa buscar(Long grupoId, Long id) {
        return despesas.findByIdAndGrupoId(id, grupoId)
                .orElseThrow(
                        () -> new NaoEncontradoException("Despesa não encontrada neste grupo."));
    }

    private void personalizar(Despesa despesa, java.util.Map<Long, java.math.BigDecimal> valores) {
        try {
            despesa.personalizar(valores);
        } catch (IllegalArgumentException e) {
            throw new ConflitoException(e.getMessage());
        }
    }

    private void bloquearComoMembro(Long grupoId) {
        grupos.buscar(grupoId);
        grupos.bloquear(grupoId);
        grupos.buscar(grupoId);
    }

    private void conferirVersao(Despesa despesa, long versao) {
        if (despesa.getVersao() != versao)
            throw new ConflitoException(
                    "Esta despesa foi alterada por outra pessoa. Recarregue os dados salvos e"
                            + " confira antes de tentar novamente.");
    }
}
