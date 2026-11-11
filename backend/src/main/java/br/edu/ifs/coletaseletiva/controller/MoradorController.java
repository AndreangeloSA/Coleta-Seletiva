package br.edu.ifs.coletaseletiva.controller;

import br.edu.ifs.coletaseletiva.dto.MoradorRequestDTO;
import br.edu.ifs.coletaseletiva.dto.MoradorResponseDTO;
import br.edu.ifs.coletaseletiva.service.MoradorService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/moradores")
public class MoradorController {
    private final MoradorService moradores;

    MoradorController(MoradorService moradores) {
        this.moradores = moradores;
    }

    @PostMapping
    ResponseEntity<MoradorResponseDTO> cadastrar(@Valid @RequestBody MoradorRequestDTO dados) {
        MoradorResponseDTO morador = new MoradorResponseDTO(moradores.cadastrar(dados));
        return ResponseEntity.created(URI.create("/api/moradores/" + morador.getId())).body(morador);
    }

    @GetMapping("/{id}")
    MoradorResponseDTO consultar(@PathVariable Long id) {
        return new MoradorResponseDTO(moradores.consultar(id));
    }

    @PutMapping("/{id}")
    MoradorResponseDTO atualizar(@PathVariable Long id, @Valid @RequestBody MoradorRequestDTO dados) {
        return new MoradorResponseDTO(moradores.atualizar(id, dados));
    }
}
