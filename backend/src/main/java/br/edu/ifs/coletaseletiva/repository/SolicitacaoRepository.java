package br.edu.ifs.coletaseletiva.repository;

import br.edu.ifs.coletaseletiva.dto.TotalPorStatusDTO;
import br.edu.ifs.coletaseletiva.entity.Solicitacao;
import br.edu.ifs.coletaseletiva.enums.SituacaoSolicitacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Long> {
    Optional<Solicitacao> findByProtocolo(String protocolo);
    boolean existsByProtocolo(String protocolo);
    boolean existsByPontoIdAndSituacaoIn(Long pontoId, List<SituacaoSolicitacao> situacoes);

    @Query("""
            select s from Solicitacao s
            where (:moradorId is null or s.morador.id = :moradorId)
              and (:pontoId is null or s.pontoId = :pontoId)
              and (:situacao is null or s.situacao = :situacao)
              and (cast(:inicio as string) is null or s.dataCriacao >= :inicio)
              and (cast(:fim as string) is null or s.dataCriacao <= :fim)
            """)
    Page<Solicitacao> findComFiltros(@Param("moradorId") Long moradorId,
                                     @Param("pontoId") Long pontoId,
                                     @Param("situacao") SituacaoSolicitacao situacao,
                                     @Param("inicio") OffsetDateTime inicio,
                                     @Param("fim") OffsetDateTime fim,
                                     Pageable pageable);

    @Query("""
            select new br.edu.ifs.coletaseletiva.dto.TotalPorStatusDTO(s.situacao, count(s))
            from Solicitacao s
            where s.dataCriacao between :inicio and :fim
            group by s.situacao
            """)
    List<TotalPorStatusDTO> contarPorStatusNoPeriodo(@Param("inicio") OffsetDateTime inicio,
                                                     @Param("fim") OffsetDateTime fim);
}