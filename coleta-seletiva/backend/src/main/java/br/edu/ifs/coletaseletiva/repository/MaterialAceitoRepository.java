package br.edu.ifs.coletaseletiva.repository;
import br.edu.ifs.coletaseletiva.entity.MaterialAceito;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MaterialAceitoRepository extends JpaRepository<MaterialAceito,Long>{boolean existsByPonto_IdAndMaterial_Id(Long pontoId,Long materialId);List<MaterialAceito> findByPonto_Id(Long pontoId);}
