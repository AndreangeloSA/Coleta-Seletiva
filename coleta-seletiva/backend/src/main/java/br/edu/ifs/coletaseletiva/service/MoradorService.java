package br.edu.ifs.coletaseletiva.service;

import br.edu.ifs.coletaseletiva.dto.MoradorRequestDTO;
import br.edu.ifs.coletaseletiva.entity.Morador;
import br.edu.ifs.coletaseletiva.repository.MoradorRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class MoradorService {
    private final MoradorRepository moradores;

    public MoradorService(MoradorRepository moradores) {
        this.moradores = moradores;
    }

    public Morador cadastrar(MoradorRequestDTO dados) {
        return salvar(new Morador(), dados);
    }

    @Transactional(readOnly = true)
    public Morador consultar(Long id) {
        return moradores.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Morador não encontrado"));
    }

    public Morador atualizar(Long id, MoradorRequestDTO dados) {
        return salvar(consultar(id), dados);
    }

    private Morador salvar(Morador morador, MoradorRequestDTO dados) {
        morador.atualizar(dados);
        try {
            return moradores.saveAndFlush(morador);
        } catch (DataIntegrityViolationException erro) {
            for (Throwable causa = erro; causa != null; causa = causa.getCause()) {
                if (causa instanceof ConstraintViolationException violacao
                        && "uk_morador_email".equalsIgnoreCase(violacao.getConstraintName())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado", erro);
                }
            }
            throw erro;
        }
    }
}

