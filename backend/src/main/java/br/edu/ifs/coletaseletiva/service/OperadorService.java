package br.edu.ifs.coletaseletiva.service;

import org.springframework.data.domain.Pageable;
import br.edu.ifs.coletaseletiva.dto.OperadorRequestDTO;
import br.edu.ifs.coletaseletiva.entity.Operador;
import br.edu.ifs.coletaseletiva.entity.OperadorPonto;
import br.edu.ifs.coletaseletiva.entity.PontoColeta;
import br.edu.ifs.coletaseletiva.repository.OperadorPontoRepository;
import br.edu.ifs.coletaseletiva.repository.OperadorRepository;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class OperadorService {

    private final OperadorRepository operadores;
    private final OperadorPontoRepository vinculos;
    private final PontoColetaService pontos;

    public OperadorService(OperadorRepository operadores,
                           OperadorPontoRepository vinculos,
                           PontoColetaService pontos) {
        this.operadores = operadores;
        this.vinculos = vinculos;
        this.pontos = pontos;
    }

    public Operador cadastrar(OperadorRequestDTO dados) {
        return salvar(new Operador(dados));
    }

    public Operador atualizar(Long id, OperadorRequestDTO dados) {
        Operador operador = buscar(id);
        operador.atualizar(dados);
        return salvar(operador);
    }

    @Transactional(readOnly = true)
    public List<Operador> listar(Pageable pageable) {
        return operadores.findAll(pageable).getContent();
    }

    @Transactional(readOnly = true)
    public Operador buscar(Long id) {
        return operadores.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Operador não encontrado"));
    }

    public OperadorPonto vincularPonto(Long operadorId, Long pontoId) {
        Operador operador = buscar(operadorId);
        PontoColeta ponto = pontos.buscar(pontoId);

        if (!ponto.isAtivo()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Não é possível vincular operador a um ponto inativo");
        }
        if (vinculos.existsByOperador_IdAndPonto_Id(operadorId, pontoId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Operador já está vinculado a este ponto");
        }

        try {
            return vinculos.saveAndFlush(new OperadorPonto(operador, ponto));
        } catch (DataIntegrityViolationException erro) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Vínculo entre operador e ponto já existe", erro);
        }
    }

    @Transactional(readOnly = true)
    public List<OperadorPonto> listarPontosVinculados(Long operadorId, Pageable pageable) {
        buscar(operadorId);
        return vinculos.findByOperador_IdOrderByPonto_Nome(operadorId, pageable);
    }

    public void desvincularPonto(Long operadorId, Long pontoId) {
        OperadorPonto vinculo = vinculos.findByOperador_IdAndPonto_Id(operadorId, pontoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Vínculo entre operador e ponto não encontrado"));
        vinculos.delete(vinculo);
    }

    private Operador salvar(Operador operador) {
        try {
            return operadores.saveAndFlush(operador);
        } catch (DataIntegrityViolationException erro) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "E-mail já cadastrado para outro operador", erro);
        }
    }
}
