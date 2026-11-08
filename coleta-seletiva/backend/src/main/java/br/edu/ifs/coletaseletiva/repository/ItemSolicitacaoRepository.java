package br.edu.ifs.coletaseletiva.repository;

import br.edu.ifs.coletaseletiva.entity.ItemSolicitacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemSolicitacaoRepository extends JpaRepository<ItemSolicitacao, Long> {

    List<ItemSolicitacao> findBySolicitacaoId(Long solicitacaoId);
}