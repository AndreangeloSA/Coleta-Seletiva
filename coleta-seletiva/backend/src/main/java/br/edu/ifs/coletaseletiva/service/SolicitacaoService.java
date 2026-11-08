package br.edu.ifs.coletaseletiva.service;

import br.edu.ifs.coletaseletiva.dto.*;
import br.edu.ifs.coletaseletiva.entity.*;
import br.edu.ifs.coletaseletiva.enums.SituacaoSolicitacao;
import br.edu.ifs.coletaseletiva.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SolicitacaoService {

    private final SolicitacaoRepository solicitacaoRepository;
    private final ItemSolicitacaoRepository itemSolicitacaoRepository;
    private final HistoricoRepository historicoRepository;

    public SolicitacaoService(SolicitacaoRepository solicitacaoRepository,
                              ItemSolicitacaoRepository itemSolicitacaoRepository,
                              HistoricoRepository historicoRepository) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.itemSolicitacaoRepository = itemSolicitacaoRepository;
        this.historicoRepository = historicoRepository;
    }

    // RF08: Agendamento de solicitação com geração de protocolo e trava de RN05
    @Transactional
    public SolicitacaoResponseDTO criarSolicitacao(SolicitacaoRequestDTO requestDTO) {
        List<SituacaoSolicitacao> situacoesAbertas = List.of(
                SituacaoSolicitacao.SOLICITADA,
                SituacaoSolicitacao.AGENDADA,
                SituacaoSolicitacao.EM_RECOLHIMENTO
        );

        // RN05: Impede nova solicitação se houver outra aberta ou em andamento para o mesmo ponto
        if (solicitacaoRepository.existsByPontoIdAndSituacaoIn(requestDTO.getPontoId(), situacoesAbertas)) {
            throw new IllegalArgumentException("Já existe uma solicitação em aberto ou em andamento para este ponto de coleta.");
        }

        Solicitacao solicitacao = new Solicitacao();
        solicitacao.setProtocolo("REC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        solicitacao.setPontoId(requestDTO.getPontoId());
        solicitacao.setSituacao(SituacaoSolicitacao.SOLICITADA);
        solicitacao.setDataCriacao(LocalDateTime.now());
        solicitacao.setDataAtualizacao(LocalDateTime.now());
        solicitacao.setObservacoes(requestDTO.getObservacoes());

        Solicitacao salva = solicitacaoRepository.save(solicitacao);

        List<ItemSolicitacao> itens = requestDTO.getItens().stream().map(dto -> {
            ItemSolicitacao item = new ItemSolicitacao();
            item.setSolicitacao(salva);
            item.setMaterialId(dto.getMaterialId());
            item.setUnidade(dto.getUnidade());
            item.setQuantidadeEstimada(dto.getQuantidadeEstimada());
            return item;
        }).collect(Collectors.toList());

        itemSolicitacaoRepository.saveAll(itens);

        registrarHistorico(salva, null, SituacaoSolicitacao.SOLICITADA, "Sistema", "Abertura de solicitação de recolhimento", null);

        return converterParaResponseDTO(salva, itens);
    }

    // RF09: Consulta paginada com filtros
    @Transactional(readOnly = true)
    public Page<SolicitacaoResponseDTO> listarComFiltros(Long pontoId, SituacaoSolicitacao situacao,
                                                         LocalDateTime inicio, LocalDateTime fim,
                                                         Pageable pageable) {
        return solicitacaoRepository.findComFiltros(pontoId, situacao, inicio, fim, pageable)
                .map(s -> converterParaResponseDTO(s, itemSolicitacaoRepository.findBySolicitacaoId(s.getId())));
    }

    @Transactional(readOnly = true)
    public SolicitacaoResponseDTO buscarPorId(Long id) {
        Solicitacao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada com o ID: " + id));
        List<ItemSolicitacao> itens = itemSolicitacaoRepository.findBySolicitacaoId(id);
        return converterParaResponseDTO(solicitacao, itens);
    }

    // RF10: Cancelamento de solicitação com registro de histórico
    @Transactional
    public SolicitacaoResponseDTO cancelarSolicitacao(Long id, CancelarSolicitacaoRequestDTO requestDTO, String responsavel) {
        Solicitacao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada com o ID: " + id));

        if (solicitacao.getSituacao() == SituacaoSolicitacao.CONCLUIDA || solicitacao.getSituacao() == SituacaoSolicitacao.CANCELADA) {
            throw new IllegalStateException("Solicitações concluídas ou já canceladas não podem ser alteradas.");
        }

        SituacaoSolicitacao situacaoAnterior = solicitacao.getSituacao();
        solicitacao.setSituacao(SituacaoSolicitacao.CANCELADA);
        solicitacao.setDataAtualizacao(LocalDateTime.now());

        Solicitacao atualizada = solicitacaoRepository.save(solicitacao);

        registrarHistorico(atualizada, situacaoAnterior, SituacaoSolicitacao.CANCELADA, responsavel, requestDTO.getMotivo(), null);

        List<ItemSolicitacao> itens = itemSolicitacaoRepository.findBySolicitacaoId(id);
        return converterParaResponseDTO(atualizada, itens);
    }

    // RF14: Consulta ao histórico de auditoria
    @Transactional(readOnly = true)
    public List<HistoricoResponseDTO> buscarHistorico(Long solicitacaoId) {
        return historicoRepository.findBySolicitacaoIdOrderByDataHoraAsc(solicitacaoId)
                .stream()
                .map(h -> {
                    HistoricoResponseDTO dto = new HistoricoResponseDTO();
                    dto.setId(h.getId());
                    dto.setSolicitacaoId(h.getSolicitacao().getId());
                    dto.setSituacaoAnterior(h.getSituacaoAnterior());
                    dto.setSituacaoNova(h.getSituacaoNova());
                    dto.setDataHora(h.getDataHora());
                    dto.setResponsavel(h.getResponsavel());
                    dto.setMotivo(h.getMotivo());
                    dto.setDadosTentativa(h.getDadosTentativa());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private void registrarHistorico(Solicitacao solicitacao, SituacaoSolicitacao anterior,
                                    SituacaoSolicitacao nova, String responsavel,
                                    String motivo, String dadosTentativa) {
        Historico historico = new Historico();
        historico.setSolicitacao(solicitacao);
        historico.setSituacaoAnterior(anterior);
        historico.setSituacaoNova(nova);
        historico.setDataHora(LocalDateTime.now());
        historico.setResponsavel(responsavel);
        historico.setMotivo(motivo);
        historico.setDadosTentativa(dadosTentativa);
        historicoRepository.save(historico);
    }

    private SolicitacaoResponseDTO converterParaResponseDTO(Solicitacao solicitacao, List<ItemSolicitacao> itens) {
        SolicitacaoResponseDTO dto = new SolicitacaoResponseDTO();
        dto.setId(solicitacao.getId());
        dto.setProtocolo(solicitacao.getProtocolo());
        dto.setPontoId(solicitacao.getPontoId());
        dto.setSituacao(solicitacao.getSituacao());
        dto.setDataCriacao(solicitacao.getDataCriacao());
        dto.setDataAtualizacao(solicitacao.getDataAtualizacao());
        dto.setObservacoes(solicitacao.getObservacoes());

        if (itens != null) {
            List<ItemSolicitacaoResponseDTO> itensDTO = itens.stream().map(i -> {
                ItemSolicitacaoResponseDTO itemDTO = new ItemSolicitacaoResponseDTO();
                itemDTO.setId(i.getId());
                itemDTO.setMaterialId(i.getMaterialId());
                itemDTO.setUnidade(i.getUnidade());
                itemDTO.setQuantidadeEstimada(i.getQuantidadeEstimada());
                itemDTO.setQuantidadeEfetiva(i.getQuantidadeEfetiva());
                return itemDTO;
            }).collect(Collectors.toList());
            dto.setItens(itensDTO);
        }

        return dto;
    }
}