package br.edu.ifs.coletaseletiva.repository;

import org.springframework.data.domain.Pageable;
import br.edu.ifs.coletaseletiva.entity.OperadorPonto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperadorPontoRepository extends JpaRepository<OperadorPonto, Long> {

    boolean existsByOperador_IdAndPonto_Id(Long operadorId, Long pontoId);

    @EntityGraph(attributePaths = "ponto")
    List<OperadorPonto> findByOperador_IdOrderByPonto_Nome(Long operadorId, Pageable pageable);

    Optional<OperadorPonto> findByOperador_IdAndPonto_Id(Long operadorId, Long pontoId);
}
