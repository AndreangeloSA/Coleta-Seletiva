package br.edu.ifs.coletaseletiva.repository;

import br.edu.ifs.coletaseletiva.entity.Operador;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperadorRepository extends JpaRepository<Operador, Long> {
    boolean existsByEmailIgnoreCase(String email);
}
