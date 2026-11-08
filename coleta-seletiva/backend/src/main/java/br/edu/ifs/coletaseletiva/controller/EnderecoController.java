package br.edu.ifs.coletaseletiva.controller;

import br.edu.ifs.coletaseletiva.dto.DadosEndereco;
import br.edu.ifs.coletaseletiva.entity.Endereco;
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
    public Endereco cadastrar(@PathVariable Long moradorId, @Valid @RequestBody DadosEndereco dados) {
        return enderecos.cadastrar(moradorId, dados);
    }

    @GetMapping("/moradores/{moradorId}/enderecos")
    public List<Endereco> listar(@PathVariable Long moradorId) {
        return enderecos.listar(moradorId);
    }

    @PutMapping("/enderecos/{id}")
    public Endereco atualizar(@PathVariable Long id, @Valid @RequestBody DadosEndereco dados) {
        return enderecos.atualizar(id, dados);
    }
}
