package br.edu.ifs.coletaseletiva.repository;

import br.edu.ifs.coletaseletiva.entity.Solicitacao;
import br.edu.ifs.coletaseletiva.enums.SituacaoSolicitacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Long> {

    Optional<Solicitacao> findByProtocolo(String protocolo);

    boolean existsByProtocolo(String protocolo);

    @Query("SELECT s FROM Solicitacao s WHERE " +
            "(:pontoId IS NULL OR s.pontoId = :pontoId) AND " +
            "(:situacao IS NULL OR s.situacao = :situacao) AND " +
            "(:inicio IS NULL OR s.dataCriacao >= :inicio) AND " +
            "(:fim IS NULL OR s.dataCriacao <= :fim)")
    Page<Solicitacao> findComFiltros(
            @Param("pontoId") Long pontoId,
            @Param("situacao") SituacaoSolicitacao situacao,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            Pageable pageable
    );

    boolean existsByPontoIdAndSituacaoIn(Long pontoId, List<SituacaoSolicitacao> situacoes);
}