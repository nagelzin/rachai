package br.com.rachai.repository;

import br.com.rachai.model.Convite;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface ConviteRepository extends JpaRepository<Convite, Long> {
    Optional<Convite> findByGrupoIdAndDestinatarioId(Long grupoId, Long destinatarioId);

    List<Convite> findByDestinatarioIdAndStatusOrderByCriadoEmDesc(Long id, Convite.Status status);

    List<Convite> findByGrupoIdOrderByCriadoEmDesc(Long id);
}
