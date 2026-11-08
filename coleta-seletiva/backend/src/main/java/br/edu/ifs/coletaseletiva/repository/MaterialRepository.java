package br.edu.ifs.coletaseletiva.repository;
import br.edu.ifs.coletaseletiva.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MaterialRepository extends JpaRepository<Material,Long>{boolean existsByNomeIgnoreCase(String nome);}
