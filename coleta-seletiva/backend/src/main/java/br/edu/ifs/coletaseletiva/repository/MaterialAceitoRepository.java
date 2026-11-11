package br.edu.ifs.coletaseletiva.repository;

import br.edu.ifs.coletaseletiva.entity.MaterialAceito;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MaterialAceitoRepository extends JpaRepository<MaterialAceito, Long> {

    boolean existsByPonto_IdAndMaterial_Id(Long pontoId, Long materialId);

    List<MaterialAceito> findByPonto_Id(Long pontoId);

    @Query("""
            select ma from MaterialAceito ma
            join fetch ma.material
            where ma.ponto.id = :pontoId and ma.material.ativo = true
            order by ma.material.nome
            """)
    List<MaterialAceito> listarMateriaisAtivosDoPonto(@Param("pontoId") Long pontoId);

    @Query("""
            select ma from MaterialAceito ma
            join fetch ma.material
            where ma.ponto.id in :pontoIds and ma.material.ativo = true
            order by ma.ponto.id, ma.material.nome
            """)
    List<MaterialAceito> listarMateriaisAtivosDosPontos(@Param("pontoIds") List<Long> pontoIds);
}
