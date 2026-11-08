package br.edu.ifs.coletaseletiva.service;

import br.edu.ifs.coletaseletiva.dto.PontoRequestDTO;
import br.edu.ifs.coletaseletiva.entity.PontoColeta;
import br.edu.ifs.coletaseletiva.repository.PontoColetaRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class PontoColetaService {
    private final PontoColetaRepository pontos;

    public PontoColetaService(PontoColetaRepository pontos) {
        this.pontos = pontos;
    }

    public PontoColeta cadastrar(PontoRequestDTO dados) {
        return salvar(new PontoColeta(), dados);
    }

    public PontoColeta atualizar(Long id, PontoRequestDTO dados) {
        return salvar(buscar(id), dados);
    }

    @Transactional(readOnly = true)
    public List<PontoColeta> listarAtivos() {
        return pontos.findAll().stream().filter(PontoColeta::isAtivo).toList();
    }

    @Transactional(readOnly = true)
    public PontoColeta buscar(Long id) {
        return pontos.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Ponto não encontrado"));
    }

    public PontoColeta inativar(Long id) {
        PontoColeta ponto = buscar(id);
        ponto.inativar();
        return ponto;
    }

    private PontoColeta salvar(PontoColeta ponto, PontoRequestDTO dados) {
        ponto.atualizar(dados.nome(), dados.endereco(), dados.contato(),
                dados.horariosFuncionamento(), dados.ativo(), dados.recolhimentoDomiciliar());
        return pontos.save(ponto);
    }
}
