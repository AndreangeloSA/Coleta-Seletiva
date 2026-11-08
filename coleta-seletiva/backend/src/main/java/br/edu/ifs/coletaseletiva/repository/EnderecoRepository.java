package br.edu.ifs.coletaseletiva.repository;

import br.edu.ifs.coletaseletiva.entity.Endereco;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnderecoRepository extends JpaRepository<Endereco, Long> {
    List<Endereco> findByMorador_IdOrderById(Long moradorId);
}
