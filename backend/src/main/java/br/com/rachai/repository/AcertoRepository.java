package br.com.rachai.repository;

import br.com.rachai.model.Acerto;

import org.springframework.data.jpa.repository.*;

import java.util.*;

public interface AcertoRepository extends JpaRepository<Acerto, Long> {
    @EntityGraph(attributePaths = {"devedor.usuario", "recebedor.usuario", "registradoPor"})
    List<Acerto> findByGrupoIdOrderByIdAsc(Long grupoId);

    Optional<Acerto> findByIdAndGrupoId(Long id, Long grupoId);

    boolean existsByDevedorIdOrRecebedorId(Long devedorId, Long recebedorId);
}
