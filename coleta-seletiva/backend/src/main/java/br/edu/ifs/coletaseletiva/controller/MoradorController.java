package br.edu.ifs.coletaseletiva.controller;

import br.edu.ifs.coletaseletiva.dto.DadosMorador;
import br.edu.ifs.coletaseletiva.entity.Morador;
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
    ResponseEntity<Morador> cadastrar(@Valid @RequestBody DadosMorador dados) {
        Morador morador = moradores.cadastrar(dados);
        return ResponseEntity.created(URI.create("/api/moradores/" + morador.getId())).body(morador);
    }

    @GetMapping("/{id}")
    Morador consultar(@PathVariable Long id) {
        return moradores.consultar(id);
    }

    @PutMapping("/{id}")
    Morador atualizar(@PathVariable Long id, @Valid @RequestBody DadosMorador dados) {
        return moradores.atualizar(id, dados);
    }
}
