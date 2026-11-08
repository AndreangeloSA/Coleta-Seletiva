package br.edu.ifs.coletaseletiva.controller;

import br.edu.ifs.coletaseletiva.dto.*;
import br.edu.ifs.coletaseletiva.enums.SituacaoSolicitacao;
import br.edu.ifs.coletaseletiva.service.SolicitacaoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/solicitacoes")
public class SolicitacaoController {

    private final SolicitacaoService solicitacaoService;

    public SolicitacaoController(SolicitacaoService solicitacaoService) {
        this.solicitacaoService = solicitacaoService;
    }

    // RF08: Criar/Agendar solicitação de recolhimento
    @PostMapping
    public ResponseEntity<SolicitacaoResponseDTO> criarSolicitacao(@Valid @RequestBody SolicitacaoRequestDTO requestDTO) {
        SolicitacaoResponseDTO criada = solicitacaoService.criarSolicitacao(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    // RF09: Consultar solicitações com paginação e filtros opcionais
    @GetMapping
    public ResponseEntity<Page<SolicitacaoResponseDTO>> listarComFiltros(
            @RequestParam(required = false) Long pontoId,
            @RequestParam(required = false) SituacaoSolicitacao situacao,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim,
            Pageable pageable) {

        Page<SolicitacaoResponseDTO> resultado = solicitacaoService.listarComFiltros(pontoId, situacao, inicio, fim, pageable);
        return ResponseEntity.ok(resultado);
    }

    // Buscar solicitação por ID
    @GetMapping("/{id}")
    public ResponseEntity<SolicitacaoResponseDTO> buscarPorId(@PathVariable Long id) {
        SolicitacaoResponseDTO dto = solicitacaoService.buscarPorId(id);
        return ResponseEntity.ok(dto);
    }

    // RF10: Cancelar solicitação
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<SolicitacaoResponseDTO> cancelarSolicitacao(
            @PathVariable Long id,
            @Valid @RequestBody CancelarSolicitacaoRequestDTO requestDTO,
            @RequestHeader(value = "X-Usuario-Responsavel", defaultValue = "UsuarioAutenticado") String responsavel) {

        SolicitacaoResponseDTO cancelada = solicitacaoService.cancelarSolicitacao(id, requestDTO, responsavel);
        return ResponseEntity.ok(cancelada);
    }

    // RF14: Consultar histórico de alterações/auditoria da solicitação
    @GetMapping("/{id}/historico")
    public ResponseEntity<List<HistoricoResponseDTO>> buscarHistorico(@PathVariable Long id) {
        List<HistoricoResponseDTO> historico = solicitacaoService.buscarHistorico(id);
        return ResponseEntity.ok(historico);
    }
}
