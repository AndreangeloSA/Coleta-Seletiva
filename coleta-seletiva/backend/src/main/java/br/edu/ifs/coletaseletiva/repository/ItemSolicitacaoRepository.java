package br.edu.ifs.coletaseletiva.repository;

import br.edu.ifs.coletaseletiva.dto.TotalPorMaterialDTO;
import br.edu.ifs.coletaseletiva.entity.ItemSolicitacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ItemSolicitacaoRepository extends JpaRepository<ItemSolicitacao, Long> {

    List<ItemSolicitacao> findBySolicitacaoId(Long solicitacaoId);

    @Query("SELECT new br.edu.ifs.coletaseletiva.dto.TotalPorMaterialDTO(m.id, m.nome, SUM(i.quantidadeEfetiva)) " +
            "FROM ItemSolicitacao i, Material m, Solicitacao s " +
            "WHERE i.materialId = m.id " +
            "AND i.solicitacao.id = s.id " +
            "AND s.situacao = br.edu.ifs.coletaseletiva.enums.SituacaoSolicitacao.CONCLUIDA " +
            "AND s.dataCriacao BETWEEN :inicio AND :fim " +
            "GROUP BY m.id, m.nome")
    List<TotalPorMaterialDTO> somarQuantidadePorMaterialNoPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
}