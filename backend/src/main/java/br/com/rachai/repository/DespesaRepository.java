package br.com.rachai.repository;

import br.com.rachai.model.Despesa;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DespesaRepository extends JpaRepository<Despesa, Long> {
    boolean existsByPagadorId(Long participanteId);

    Optional<Despesa> findByIdAndGrupoId(Long id, Long grupoId);

    @EntityGraph(attributePaths = {"pagador.usuario", "envolvidos.participante.usuario"})
    List<Despesa> findByGrupoIdOrderByDataDescIdDesc(Long grupoId);
}
