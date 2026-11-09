package br.edu.ifs.coletaseletiva.controller;

import br.edu.ifs.coletaseletiva.dto.*;
import br.edu.ifs.coletaseletiva.service.MaterialAceitoService;
import br.edu.ifs.coletaseletiva.service.MaterialService;
import br.edu.ifs.coletaseletiva.service.PontoColetaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CatalogoController {
    private final MaterialService materiais;
    private final PontoColetaService pontos;
    private final MaterialAceitoService vinculos;

    public CatalogoController(MaterialService materiais, PontoColetaService pontos,
                              MaterialAceitoService vinculos) {
        this.materiais = materiais;
        this.pontos = pontos;
        this.vinculos = vinculos;
    }

    @PostMapping("/materiais")
    @ResponseStatus(HttpStatus.CREATED)
    public MaterialResponseDTO criarMaterial(@Valid @RequestBody MaterialRequestDTO dados) {
        return new MaterialResponseDTO(materiais.cadastrar(dados));
    }

    @GetMapping("/materiais")
    public List<MaterialResponseDTO> listarMateriais() {
        return materiais.listar().stream().map(MaterialResponseDTO::new).toList();
    }

    @GetMapping("/materiais/{id}")
    public MaterialResponseDTO consultarMaterial(@PathVariable Long id) {
        return new MaterialResponseDTO(materiais.buscar(id));
    }

    @PutMapping("/materiais/{id}")
    public MaterialResponseDTO atualizarMaterial(@PathVariable Long id,
                                                  @Valid @RequestBody MaterialRequestDTO dados) {
        return new MaterialResponseDTO(materiais.atualizar(id, dados));
    }

    @PatchMapping("/materiais/{id}/inativacao")
    public MaterialResponseDTO inativarMaterial(@PathVariable Long id) {
        return new MaterialResponseDTO(materiais.inativar(id));
    }

    @PostMapping("/pontos")
    @ResponseStatus(HttpStatus.CREATED)
    public PontoResponseDTO criarPonto(@Valid @RequestBody PontoRequestDTO dados) {
        return new PontoResponseDTO(pontos.cadastrar(dados));
    }

    @GetMapping("/pontos")
    public Page<PontoConsultaResponseDTO> listarPontos(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String municipio,
            @RequestParam(required = false) String bairro,
            @RequestParam(required = false) Long materialId,
            @RequestParam(required = false) String material,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return pontos.pesquisarAtivos(nome, municipio, bairro, materialId, material, pagina, tamanho);
    }

    @GetMapping("/pontos/{id}")
    public PontoConsultaResponseDTO consultarPonto(@PathVariable Long id) {
        return pontos.consultarComMateriais(id);
    }

    @PutMapping("/pontos/{id}")
    public PontoResponseDTO atualizarPonto(@PathVariable Long id,
                                            @Valid @RequestBody PontoRequestDTO dados) {
        return new PontoResponseDTO(pontos.atualizar(id, dados));
    }

    @PatchMapping("/pontos/{id}/inativacao")
    public PontoResponseDTO inativarPonto(@PathVariable Long id) {
        return new PontoResponseDTO(pontos.inativar(id));
    }

    @PostMapping("/pontos/{pontoId}/materiais/{materialId}")
    @ResponseStatus(HttpStatus.CREATED)
    public MaterialAceitoResponseDTO vincular(@PathVariable Long pontoId,
                                               @PathVariable Long materialId,
                                               @Valid @RequestBody MaterialAceitoRequestDTO dados) {
        return new MaterialAceitoResponseDTO(vinculos.vincular(pontoId, materialId, dados));
    }

    @GetMapping("/pontos/{pontoId}/materiais")
    public List<MaterialAceitoResponseDTO> listarVinculos(@PathVariable Long pontoId) {
        return vinculos.listarDoPonto(pontoId).stream().map(MaterialAceitoResponseDTO::new).toList();
    }

    @DeleteMapping("/pontos/{pontoId}/materiais/{materialId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removerVinculo(@PathVariable Long pontoId, @PathVariable Long materialId) {
        vinculos.remover(pontoId, materialId);
    }
}
