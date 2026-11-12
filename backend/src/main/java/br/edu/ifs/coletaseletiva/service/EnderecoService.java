package br.edu.ifs.coletaseletiva.service;

import org.springframework.data.domain.Pageable;
import br.edu.ifs.coletaseletiva.dto.EnderecoRequestDTO;
import br.edu.ifs.coletaseletiva.entity.Endereco;
import br.edu.ifs.coletaseletiva.repository.EnderecoRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class EnderecoService {
    private final EnderecoRepository enderecos;
    private final MoradorService moradores;

    public EnderecoService(EnderecoRepository enderecos, MoradorService moradores) {
        this.enderecos = enderecos;
        this.moradores = moradores;
    }

    public Endereco cadastrar(Long moradorId, EnderecoRequestDTO dados) {
        return enderecos.save(new Endereco(moradores.consultar(moradorId), dados));
    }

    @Transactional(readOnly = true)
    public List<Endereco> listar(Long moradorId, Pageable pageable) {
        moradores.consultar(moradorId);
        return enderecos.findByMorador_IdOrderById(moradorId, pageable);
    }

    public Endereco atualizar(Long id, EnderecoRequestDTO dados) {
        Endereco endereco = enderecos.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Endereço não encontrado"));
        endereco.atualizar(dados);
        return endereco;
    }
}
