package br.com.rachai.service;

import br.com.rachai.dto.*;
import br.com.rachai.exception.*;
import br.com.rachai.model.*;
import br.com.rachai.repository.*;
import br.com.rachai.security.UsuarioAtual;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ConviteService {
    private final GrupoService grupos;
    private final UsuarioRepository usuarios;
    private final ConviteRepository convites;
    private final GrupoParticipanteRepository membros;
    private final UsuarioAtual atual;

    public ConviteService(
            GrupoService grupos,
            UsuarioRepository usuarios,
            ConviteRepository convites,
            GrupoParticipanteRepository membros,
            UsuarioAtual atual) {
        this.grupos = grupos;
        this.usuarios = usuarios;
        this.convites = convites;
        this.membros = membros;
        this.atual = atual;
    }

    @Transactional
    public ConviteResponse convidar(Long grupoId, ConvidarRequest r) {
        var grupo = grupos.administrador(grupoId);
        grupos.bloquear(grupoId);
        var usuario =
                usuarios.findByEmail(Usuario.normalizarEmail(r.email()))
                        .orElseThrow(
                                () ->
                                        new NaoEncontradoException(
                                                "Peça à pessoa para criar uma conta com esse e-mail"
                                                        + " antes de convidá-la."));
        if (membros.existsByGrupoIdAndUsuarioId(grupoId, usuario.getId()))
            throw new ConflitoException("Essa pessoa já participa do grupo.");
        var convite =
                convites.findByGrupoIdAndDestinatarioId(grupoId, usuario.getId()).orElse(null);
        if (convite == null) convite = new Convite(grupo, usuario);
        else {
            if (convite.getStatus() == Convite.Status.PENDENTE)
                throw new ConflitoException("Já existe um convite pendente para essa pessoa.");
            convite.reabrir();
        }
        return ConviteResponse.from(convites.saveAndFlush(convite));
    }

    public List<ConviteResponse> recebidos() {
        return convites
                .findByDestinatarioIdAndStatusOrderByCriadoEmDesc(
                        atual.obter().getId(), Convite.Status.PENDENTE)
                .stream()
                .map(ConviteResponse::from)
                .toList();
    }

    public List<ConviteResponse> enviados(Long grupoId) {
        grupos.administrador(grupoId);
        return convites.findByGrupoIdOrderByCriadoEmDesc(grupoId).stream()
                .map(ConviteResponse::from)
                .toList();
    }

    @Transactional
    public ConviteResponse responder(Long id, boolean aceitar) {
        var usuario = atual.obter();
        var convite =
                convites.findById(id)
                        .filter(c -> c.getDestinatario().getId().equals(usuario.getId()))
                        .orElseThrow(() -> new NaoEncontradoException("Convite não encontrado."));
        grupos.bloquear(convite.getGrupo().getId());
        // Recarrega estado após adquirir o lock (a primeira leitura pode ter aguardado outra
        // transação).
        entityManager.refresh(convite);
        if (convite.getStatus() != Convite.Status.PENDENTE)
            throw new ConflitoException("Esse convite já foi respondido.");
        if (aceitar
                && !membros.existsByGrupoIdAndUsuarioId(
                        convite.getGrupo().getId(), usuario.getId()))
            membros.saveAndFlush(new GrupoParticipante(convite.getGrupo(), usuario));
        convite.responder(aceitar);
        return ConviteResponse.from(convites.saveAndFlush(convite));
    }

    @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager entityManager;
}
