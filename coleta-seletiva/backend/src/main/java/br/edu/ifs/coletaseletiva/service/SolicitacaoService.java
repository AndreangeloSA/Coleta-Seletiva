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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.math.BigDecimal;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SolicitacaoService {

    private final SolicitacaoRepository solicitacaoRepository;
    private final ItemSolicitacaoRepository itemSolicitacaoRepository;
    private final HistoricoRepository historicoRepository;
    private final MoradorRepository moradorRepository;
    private final EnderecoRepository enderecoRepository;
    private final PontoColetaRepository pontoColetaRepository;
    private final MaterialAceitoRepository materialAceitoRepository;

    public SolicitacaoService(SolicitacaoRepository solicitacaoRepository,
                              ItemSolicitacaoRepository itemSolicitacaoRepository,
                              HistoricoRepository historicoRepository,
                              MoradorRepository moradorRepository,
                              EnderecoRepository enderecoRepository,
                              PontoColetaRepository pontoColetaRepository,
                              MaterialAceitoRepository materialAceitoRepository) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.itemSolicitacaoRepository = itemSolicitacaoRepository;
        this.historicoRepository = historicoRepository;
        this.moradorRepository = moradorRepository;
        this.enderecoRepository = enderecoRepository;
        this.pontoColetaRepository = pontoColetaRepository;
        this.materialAceitoRepository = materialAceitoRepository;
    }

    // RF08: cria solicitação validando morador, endereço, ponto e materiais aceitos.
    @Transactional
    public SolicitacaoResponseDTO criarSolicitacao(SolicitacaoRequestDTO requestDTO) {
        Morador morador = moradorRepository.findById(requestDTO.getMoradorId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Morador não encontrado."));

        Endereco endereco = enderecoRepository.findById(requestDTO.getEnderecoId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Endereço não encontrado."));

        if (!endereco.getMoradorId().equals(morador.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "O endereço informado não pertence ao morador.");
        }

        PontoColeta ponto = pontoColetaRepository.findById(requestDTO.getPontoId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ponto de coleta não encontrado."));

        if (!ponto.isAtivo()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Não é possível solicitar coleta em um ponto inativo.");
        }

        if (requestDTO.getItens() == null || requestDTO.getItens().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "A solicitação deve conter pelo menos um material.");
        }

        // RN05: mantém a regra já existente de uma solicitação aberta por ponto.
        List<SituacaoSolicitacao> situacoesAbertas = List.of(
                SituacaoSolicitacao.SOLICITADA,
                SituacaoSolicitacao.AGENDADA,
                SituacaoSolicitacao.EM_RECOLHIMENTO
        );
        if (solicitacaoRepository.existsByPontoIdAndSituacaoIn(requestDTO.getPontoId(), situacoesAbertas)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe uma solicitação em aberto ou em andamento para este ponto de coleta.");
        }

        // Carrega os materiais realmente aceitos pelo ponto e valida cada item antes de persistir.
        Map<Long, MaterialAceito> materiaisAceitos = materialAceitoRepository
                .listarMateriaisAtivosDoPonto(ponto.getId())
                .stream()
                .collect(Collectors.toMap(MaterialAceito::getMaterialId, Function.identity()));

        Set<Long> materiaisInformados = new HashSet<>();
        for (ItemSolicitacaoRequestDTO itemDTO : requestDTO.getItens()) {
            if (itemDTO.getMaterialId() == null || !materiaisInformados.add(itemDTO.getMaterialId())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Cada material deve ser informado uma única vez na solicitação.");
            }

            MaterialAceito materialAceito = materiaisAceitos.get(itemDTO.getMaterialId());
            if (materialAceito == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "O material de ID " + itemDTO.getMaterialId()
                                + " não está ativo ou não é aceito pelo ponto de coleta informado.");
            }

            if (itemDTO.getUnidade() == null
                    || !materialAceito.getUnidadeMedida().equalsIgnoreCase(itemDTO.getUnidade().strip())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "A unidade informada para o material de ID " + itemDTO.getMaterialId()
                                + " deve ser " + materialAceito.getUnidadeMedida() + ".");
            }

            BigDecimal limite = materialAceito.getLimitePorSolicitacao();
            if (limite != null && itemDTO.getQuantidadeEstimada() != null
                    && itemDTO.getQuantidadeEstimada().compareTo(limite) > 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "A quantidade do material de ID " + itemDTO.getMaterialId()
                                + " excede o limite por solicitação (" + limite + ").");
            }
        }

        Solicitacao solicitacao = new Solicitacao();
        String protocolo;
        do {
            protocolo = "REC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (solicitacaoRepository.existsByProtocolo(protocolo));

        solicitacao.setProtocolo(protocolo);
        solicitacao.setMorador(morador);
        solicitacao.setPontoId(ponto.getId());

        // A solicitação guarda uma cópia do endereço para preservar o histórico (RN04).
        solicitacao.setLogradouro(endereco.getLogradouro());
        solicitacao.setNumero(endereco.getNumero());
        solicitacao.setBairro(endereco.getBairro());
        solicitacao.setMunicipio(endereco.getMunicipio());
        solicitacao.setUf(endereco.getUf());
        solicitacao.setCep(endereco.getCep());
        solicitacao.setComplemento(endereco.getComplemento());

        solicitacao.setSituacao(SituacaoSolicitacao.SOLICITADA);
        LocalDateTime agora = LocalDateTime.now();
        solicitacao.setDataCriacao(agora);
        solicitacao.setDataAtualizacao(agora);
        solicitacao.setObservacoes(requestDTO.getObservacoes());

        Solicitacao salva = solicitacaoRepository.save(solicitacao);

        List<ItemSolicitacao> itens = requestDTO.getItens().stream().map(dto -> {
            ItemSolicitacao item = new ItemSolicitacao();
            item.setSolicitacao(salva);
            item.setMaterialId(dto.getMaterialId());
            item.setUnidade(dto.getUnidade().strip());
            item.setQuantidadeEstimada(dto.getQuantidadeEstimada());
            return item;
        }).collect(Collectors.toList());

        List<ItemSolicitacao> itensSalvos = itemSolicitacaoRepository.saveAll(itens);

        registrarHistorico(salva, null, SituacaoSolicitacao.SOLICITADA,
                "Sistema", "Abertura de solicitação de recolhimento", null);

        return converterParaResponseDTO(salva, itensSalvos);
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

    // RF11 e RF12: Registrar conclusão do recolhimento com quantidades efetivas
    @Transactional
    public SolicitacaoResponseDTO concluirSolicitacao(Long id, ConcluirSolicitacaoRequestDTO requestDTO, String responsavel) {
        Solicitacao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada com o ID: " + id));

        if (solicitacao.getSituacao() == SituacaoSolicitacao.CONCLUIDA || solicitacao.getSituacao() == SituacaoSolicitacao.CANCELADA || solicitacao.getSituacao() == SituacaoSolicitacao.RECUSADA) {
            throw new IllegalStateException("Solicitações já finalizadas (concluídas, recusadas ou canceladas) não podem ser alteradas.");
        }

        List<ItemSolicitacao> itensExistentes = itemSolicitacaoRepository.findBySolicitacaoId(id);

        for (ItemConclusaoDTO itemDTO : requestDTO.getItens()) {
            ItemSolicitacao item = itensExistentes.stream()
                    .filter(i -> i.getId().equals(itemDTO.getItemId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Item ID " + itemDTO.getItemId() + " não pertence a esta solicitação."));
            item.setQuantidadeEfetiva(itemDTO.getQuantidadeEfetiva());
        }

        itemSolicitacaoRepository.saveAll(itensExistentes);

        SituacaoSolicitacao anterior = solicitacao.getSituacao();
        solicitacao.setSituacao(SituacaoSolicitacao.CONCLUIDA);
        solicitacao.setDataAtualizacao(LocalDateTime.now());
        if (requestDTO.getObservacoes() != null && !requestDTO.getObservacoes().isBlank()) {
            solicitacao.setObservacoes(solicitacao.getObservacoes() + " | Conclusão: " + requestDTO.getObservacoes());
        }

        Solicitacao atualizada = solicitacaoRepository.save(solicitacao);

        registrarHistorico(atualizada, anterior, SituacaoSolicitacao.CONCLUIDA, responsavel, "Recolhimento concluído com sucesso", null);

        return converterParaResponseDTO(atualizada, itensExistentes);
    }

    // RF13: Registrar tentativa frustrada de recolhimento
    @Transactional
    public SolicitacaoResponseDTO registrarFrustracao(Long id, RegistrarFrustracaoRequestDTO requestDTO, String responsavel) {
        Solicitacao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada com o ID: " + id));

        if (solicitacao.getSituacao() == SituacaoSolicitacao.CONCLUIDA || solicitacao.getSituacao() == SituacaoSolicitacao.CANCELADA || solicitacao.getSituacao() == SituacaoSolicitacao.RECUSADA) {
            throw new IllegalStateException("Solicitações já finalizadas não podem ter tentativa frustrada registrada.");
        }

        SituacaoSolicitacao anterior = solicitacao.getSituacao();
        solicitacao.setSituacao(SituacaoSolicitacao.RECUSADA);
        solicitacao.setDataAtualizacao(LocalDateTime.now());

        Solicitacao atualizada = solicitacaoRepository.save(solicitacao);

        registrarHistorico(atualizada, anterior, SituacaoSolicitacao.RECUSADA, responsavel, requestDTO.getMotivo(), requestDTO.getDadosTentativa());

        List<ItemSolicitacao> itens = itemSolicitacaoRepository.findBySolicitacaoId(id);
        return converterParaResponseDTO(atualizada, itens);
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
        dto.setMoradorId(solicitacao.getMorador() != null ? solicitacao.getMorador().getId() : null);
        dto.setPontoId(solicitacao.getPontoId());
        dto.setLogradouro(solicitacao.getLogradouro());
        dto.setNumero(solicitacao.getNumero());
        dto.setBairro(solicitacao.getBairro());
        dto.setMunicipio(solicitacao.getMunicipio());
        dto.setUf(solicitacao.getUf());
        dto.setCep(solicitacao.getCep());
        dto.setComplemento(solicitacao.getComplemento());
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

    // RF15: Gerar resumo gerencial de solicitações e materiais por período
    @Transactional(readOnly = true)
    public ResumoGerencialResponseDTO obterResumoGerencial(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio == null || fim == null) {
            throw new IllegalArgumentException("As datas de início e fim são obrigatórias para gerar o resumo gerencial.");
        }
        if (inicio.isAfter(fim)) {
            throw new IllegalArgumentException("A data de início não pode ser posterior à data de fim.");
        }

        List<TotalPorStatusDTO> totaisStatus = solicitacaoRepository.contarPorStatusNoPeriodo(inicio, fim);
        List<TotalPorMaterialDTO> totaisMaterial = itemSolicitacaoRepository.somarQuantidadePorMaterialNoPeriodo(inicio, fim);

        return new ResumoGerencialResponseDTO(inicio, fim, totaisStatus, totaisMaterial);
    }
}