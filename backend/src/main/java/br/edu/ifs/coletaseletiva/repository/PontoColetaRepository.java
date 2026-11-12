package br.edu.ifs.coletaseletiva.repository;

import br.edu.ifs.coletaseletiva.entity.PontoColeta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PontoColetaRepository extends JpaRepository<PontoColeta, Long> {

    /**
     * RF06: pesquisa apenas pontos ativos. Município e bairro são pesquisados no campo
     * endereco, que é o campo textual adotado pelo modelo atual para o endereço do ponto.
     */
    @Query(value = """
            select p from PontoColeta p
            where p.ativo = true
              and (:nome is null or lower(p.nome) like lower(concat('%', cast(:nome as string), '%')))
              and (:municipio is null or lower(coalesce(p.municipio, p.endereco)) like lower(concat('%', cast(:municipio as string), '%')))
              and (:bairro is null or lower(coalesce(p.bairro, p.endereco)) like lower(concat('%', cast(:bairro as string), '%')))
              and (:materialId is null or exists (
                    select ma.id from MaterialAceito ma
                    where ma.ponto = p and ma.material.ativo = true and ma.material.id = :materialId
              ))
              and (:material is null or exists (
                    select ma.id from MaterialAceito ma
                    where ma.ponto = p and ma.material.ativo = true
                      and lower(ma.material.nome) like lower(concat('%', cast(:material as string), '%'))
              ))
            """,
            countQuery = """
            select count(p) from PontoColeta p
            where p.ativo = true
              and (:nome is null or lower(p.nome) like lower(concat('%', cast(:nome as string), '%')))
              and (:municipio is null or lower(coalesce(p.municipio, p.endereco)) like lower(concat('%', cast(:municipio as string), '%')))
              and (:bairro is null or lower(coalesce(p.bairro, p.endereco)) like lower(concat('%', cast(:bairro as string), '%')))
              and (:materialId is null or exists (
                    select ma.id from MaterialAceito ma
                    where ma.ponto = p and ma.material.ativo = true and ma.material.id = :materialId
              ))
              and (:material is null or exists (
                    select ma.id from MaterialAceito ma
                    where ma.ponto = p and ma.material.ativo = true
                      and lower(ma.material.nome) like lower(concat('%', cast(:material as string), '%'))
              ))
            """)
    Page<PontoColeta> pesquisarAtivos(
            @Param("nome") String nome,
            @Param("municipio") String municipio,
            @Param("bairro") String bairro,
            @Param("materialId") Long materialId,
            @Param("material") String material,
            Pageable pageable
    );
}