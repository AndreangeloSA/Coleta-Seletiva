package br.edu.ifs.coletaseletiva.repository;

import br.edu.ifs.coletaseletiva.entity.Morador;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MoradorRepository extends JpaRepository<Morador, Long> {
}

