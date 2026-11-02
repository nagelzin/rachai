package br.com.rachai.repository;

import br.com.rachai.model.GrupoParticipante;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface GrupoParticipanteRepository extends JpaRepository<GrupoParticipante, Long> {
    @EntityGraph(attributePaths = "usuario")
    List<GrupoParticipante> findByGrupoIdOrderByIdAsc(Long grupoId);

    boolean existsByGrupoIdAndUsuarioId(Long grupoId, Long usuarioId);

    Optional<GrupoParticipante> findByIdAndGrupoId(Long id, Long grupoId);
}
