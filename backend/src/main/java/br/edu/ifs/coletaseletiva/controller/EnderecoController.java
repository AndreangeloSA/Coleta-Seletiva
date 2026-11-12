package br.edu.ifs.coletaseletiva.controller;

import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Pageable;
import br.edu.ifs.coletaseletiva.dto.EnderecoRequestDTO;
import br.edu.ifs.coletaseletiva.dto.EnderecoResponseDTO;
import br.edu.ifs.coletaseletiva.service.EnderecoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class EnderecoController {
    private final EnderecoService enderecos;

    public EnderecoController(EnderecoService enderecos) {
        this.enderecos = enderecos;
    }

    @PostMapping("/moradores/{moradorId}/enderecos")
    @ResponseStatus(HttpStatus.CREATED)
    public EnderecoResponseDTO cadastrar(@PathVariable Long moradorId, @Valid @RequestBody EnderecoRequestDTO dados) {
        return new EnderecoResponseDTO(enderecos.cadastrar(moradorId, dados));
    }

    @GetMapping("/moradores/{moradorId}/enderecos")
    public List<EnderecoResponseDTO> listar(@PathVariable Long moradorId, @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return enderecos.listar(moradorId, pageable).stream().map(EnderecoResponseDTO::new).toList();
    }

    @PutMapping("/enderecos/{id}")
    public EnderecoResponseDTO atualizar(@PathVariable Long id, @Valid @RequestBody EnderecoRequestDTO dados) {
        return new EnderecoResponseDTO(enderecos.atualizar(id, dados));
    }
}
