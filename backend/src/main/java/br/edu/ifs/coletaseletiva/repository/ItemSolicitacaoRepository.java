package br.edu.ifs.coletaseletiva.repository;

import br.edu.ifs.coletaseletiva.dto.TotalPorMaterialDTO;
import br.edu.ifs.coletaseletiva.entity.ItemSolicitacao;
import br.edu.ifs.coletaseletiva.enums.SituacaoSolicitacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface ItemSolicitacaoRepository extends JpaRepository<ItemSolicitacao, Long> {

    List<ItemSolicitacao> findBySolicitacaoId(Long solicitacaoId);

    @Query("""
            select case when count(i) > 0 then true else false end
            from ItemSolicitacao i
            where i.materialId = :materialId
              and i.solicitacao.pontoId = :pontoId
              and i.solicitacao.situacao in :situacoes
            """)
    boolean existeItemEmSolicitacaoAberta(@Param("pontoId") Long pontoId,
                                          @Param("materialId") Long materialId,
                                          @Param("situacoes") List<SituacaoSolicitacao> situacoes);

    @Query("""
            select new br.edu.ifs.coletaseletiva.dto.TotalPorMaterialDTO(
                m.id, m.nome, i.unidade, sum(i.quantidadeEfetiva))
            from ItemSolicitacao i, Material m, Historico h
            where i.materialId = m.id
              and h.solicitacao = i.solicitacao
              and h.situacaoNova = br.edu.ifs.coletaseletiva.enums.SituacaoSolicitacao.CONCLUIDA
              and i.solicitacao.situacao = br.edu.ifs.coletaseletiva.enums.SituacaoSolicitacao.CONCLUIDA
              and h.dataHora between :inicio and :fim
              and i.quantidadeEfetiva is not null
            group by m.id, m.nome, i.unidade
            order by m.nome
            """)
    List<TotalPorMaterialDTO> somarQuantidadePorMaterialNoPeriodo(
            @Param("inicio") OffsetDateTime inicio, @Param("fim") OffsetDateTime fim);
}
