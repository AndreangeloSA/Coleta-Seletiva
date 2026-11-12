package br.edu.ifs.coletaseletiva.repository;

import org.springframework.data.domain.Pageable;
import br.edu.ifs.coletaseletiva.entity.Historico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoricoRepository extends JpaRepository<Historico, Long> {

    List<Historico> findBySolicitacaoIdOrderByDataHoraAsc(Long solicitacaoId, Pageable pageable);
}