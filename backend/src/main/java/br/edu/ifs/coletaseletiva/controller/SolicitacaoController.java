package br.edu.ifs.coletaseletiva.controller;

import org.springframework.data.web.PageableDefault;
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

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/solicitacoes")
public class SolicitacaoController {
    private final SolicitacaoService solicitacaoService;

    public SolicitacaoController(SolicitacaoService solicitacaoService) {
        this.solicitacaoService = solicitacaoService;
    }

    @PostMapping
    public ResponseEntity<SolicitacaoResponseDTO> criarSolicitacao(
            @Valid @RequestBody SolicitacaoRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(solicitacaoService.criarSolicitacao(requestDTO));
    }

    @GetMapping
    public ResponseEntity<Page<SolicitacaoResponseDTO>> listarComFiltros(
            @RequestParam(required = false) Long moradorId,
            @RequestParam(required = false) Long pontoId,
            @RequestParam(required = false) SituacaoSolicitacao situacao,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fim,
            Pageable pageable) {
        return ResponseEntity.ok(solicitacaoService.listarComFiltros(
                moradorId, pontoId, situacao, inicio, fim, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitacaoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(solicitacaoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SolicitacaoResponseDTO> atualizarAntesDoAgendamento(
            @PathVariable Long id, @Valid @RequestBody SolicitacaoRequestDTO requestDTO) {
        return ResponseEntity.ok(solicitacaoService.atualizarAntesDoAgendamento(id, requestDTO));
    }

    @PostMapping("/{id}/agendar")
    public ResponseEntity<SolicitacaoResponseDTO> agendar(
            @PathVariable Long id, @Valid @RequestBody AgendarSolicitacaoRequestDTO dados,
            @RequestHeader(value = "X-Usuario-Responsavel", defaultValue = "Operador (protótipo)") String responsavel) {
        return ResponseEntity.ok(solicitacaoService.agendar(id, dados, responsavel));
    }

    @PostMapping("/{id}/recusar")
    public ResponseEntity<SolicitacaoResponseDTO> recusar(
            @PathVariable Long id, @Valid @RequestBody RecusarSolicitacaoRequestDTO dados,
            @RequestHeader(value = "X-Usuario-Responsavel", defaultValue = "Operador (protótipo)") String responsavel) {
        return ResponseEntity.ok(solicitacaoService.recusar(id, dados, responsavel));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<SolicitacaoResponseDTO> cancelar(
            @PathVariable Long id, @Valid @RequestBody CancelarSolicitacaoRequestDTO dados,
            @RequestHeader(value = "X-Usuario-Responsavel", defaultValue = "Morador (protótipo)") String responsavel) {
        return ResponseEntity.ok(solicitacaoService.cancelarSolicitacao(id, dados, responsavel));
    }

    @PostMapping("/{id}/iniciar")
    public ResponseEntity<SolicitacaoResponseDTO> iniciar(
            @PathVariable Long id, @Valid @RequestBody VersaoRequestDTO dados,
            @RequestHeader(value = "X-Usuario-Responsavel", defaultValue = "Operador (protótipo)") String responsavel) {
        return ResponseEntity.ok(solicitacaoService.iniciar(id, dados, responsavel));
    }

    @PatchMapping("/{id}/concluir")
    public ResponseEntity<SolicitacaoResponseDTO> concluir(
            @PathVariable Long id, @Valid @RequestBody ConcluirSolicitacaoRequestDTO dados,
            @RequestHeader(value = "X-Usuario-Responsavel", defaultValue = "Operador (protótipo)") String responsavel) {
        return ResponseEntity.ok(solicitacaoService.concluirSolicitacao(id, dados, responsavel));
    }

    @PatchMapping("/{id}/frustrada")
    public ResponseEntity<SolicitacaoResponseDTO> registrarFrustracao(
            @PathVariable Long id, @Valid @RequestBody RegistrarFrustracaoRequestDTO dados,
            @RequestHeader(value = "X-Usuario-Responsavel", defaultValue = "Operador (protótipo)") String responsavel) {
        return ResponseEntity.ok(solicitacaoService.registrarFrustracao(id, dados, responsavel));
    }

    @PatchMapping("/{id}/reagendar")
    public ResponseEntity<SolicitacaoResponseDTO> reagendar(
            @PathVariable Long id, @Valid @RequestBody ReagendarSolicitacaoRequestDTO dados,
            @RequestHeader(value = "X-Usuario-Responsavel", defaultValue = "Operador (protótipo)") String responsavel) {
        return ResponseEntity.ok(solicitacaoService.reagendar(id, dados, responsavel));
    }

    @GetMapping("/{id}/historico")
    public ResponseEntity<List<HistoricoResponseDTO>> buscarHistorico(@PathVariable Long id, @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(solicitacaoService.buscarHistorico(id, pageable));
    }

    @GetMapping("/resumo-gerencial")
    public ResponseEntity<ResumoGerencialResponseDTO> obterResumoGerencial(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fim) {
        return ResponseEntity.ok(solicitacaoService.obterResumoGerencial(inicio, fim));
    }
}
