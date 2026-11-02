package br.com.rachai.repository;

import br.com.rachai.model.Grupo;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface GrupoRepository extends JpaRepository<Grupo, Long> {
    @Query(
            "select g from Grupo g where exists (select p.id from GrupoParticipante p where"
                    + " p.grupo=g and p.usuario.id=:usuarioId) order by g.criadoEm desc, g.id desc")
    List<Grupo> listarDoUsuario(@Param("usuarioId") Long usuarioId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from Grupo g where g.id=:id")
    Optional<Grupo> bloquear(@Param("id") Long id);
}
