package br.edu.ifs.coletaseletiva.controller;

import br.edu.ifs.coletaseletiva.dto.OperadorPontoResponseDTO;
import br.edu.ifs.coletaseletiva.dto.OperadorRequestDTO;
import br.edu.ifs.coletaseletiva.dto.OperadorResponseDTO;
import br.edu.ifs.coletaseletiva.service.OperadorService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/operadores")
public class OperadorController {

    private final OperadorService operadores;

    public OperadorController(OperadorService operadores) {
        this.operadores = operadores;
    }

    @PostMapping
    public ResponseEntity<OperadorResponseDTO> cadastrar(@Valid @RequestBody OperadorRequestDTO dados) {
        OperadorResponseDTO operador = new OperadorResponseDTO(operadores.cadastrar(dados));
        return ResponseEntity.created(URI.create("/api/operadores/" + operador.id())).body(operador);
    }

    @GetMapping
    public List<OperadorResponseDTO> listar() {
        return operadores.listar().stream().map(OperadorResponseDTO::new).toList();
    }

    @GetMapping("/{id}")
    public OperadorResponseDTO consultar(@PathVariable Long id) {
        return new OperadorResponseDTO(operadores.buscar(id));
    }

    @PutMapping("/{id}")
    public OperadorResponseDTO atualizar(@PathVariable Long id,
                                          @Valid @RequestBody OperadorRequestDTO dados) {
        return new OperadorResponseDTO(operadores.atualizar(id, dados));
    }

    @PostMapping("/{operadorId}/pontos/{pontoId}")
    @ResponseStatus(HttpStatus.CREATED)
    public OperadorPontoResponseDTO vincularPonto(@PathVariable Long operadorId,
                                                   @PathVariable Long pontoId) {
        return new OperadorPontoResponseDTO(operadores.vincularPonto(operadorId, pontoId));
    }

    @GetMapping("/{operadorId}/pontos")
    public List<OperadorPontoResponseDTO> listarPontos(@PathVariable Long operadorId) {
        return operadores.listarPontosVinculados(operadorId).stream()
                .map(OperadorPontoResponseDTO::new).toList();
    }

    @DeleteMapping("/{operadorId}/pontos/{pontoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desvincularPonto(@PathVariable Long operadorId, @PathVariable Long pontoId) {
        operadores.desvincularPonto(operadorId, pontoId);
    }
}
