package br.com.rachai.repository;

import br.com.rachai.model.DespesaParticipante;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DespesaParticipanteRepository extends JpaRepository<DespesaParticipante, Long> {
    boolean existsByParticipanteId(Long id);
}
