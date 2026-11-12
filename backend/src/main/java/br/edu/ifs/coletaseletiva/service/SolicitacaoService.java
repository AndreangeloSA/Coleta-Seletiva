package br.edu.ifs.coletaseletiva.service;

import br.edu.ifs.coletaseletiva.dto.*;
import br.edu.ifs.coletaseletiva.entity.*;
import br.edu.ifs.coletaseletiva.enums.SituacaoSolicitacao;
import br.edu.ifs.coletaseletiva.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SolicitacaoService {
    private static final ZoneId FUSO_DO_PROJETO = ZoneId.of("America/Maceio");
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

    // RF08: cria solicitação, itens e histórico na mesma transação.
    @Transactional
    public SolicitacaoResponseDTO criarSolicitacao(SolicitacaoRequestDTO requestDTO) {
        Morador morador = moradorRepository.findById(requestDTO.getMoradorId())
                .orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Morador não encontrado."));
        Endereco endereco = enderecoRepository.findById(requestDTO.getEnderecoId())
                .orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Endereço não encontrado."));
        validarTitularidadeEndereco(endereco, morador);
        PontoColeta ponto = buscarPontoCompativel(requestDTO.getPontoId());
        Map<Long, MaterialAceito> aceitos = validarItens(ponto.getId(), requestDTO.getItens());

        Solicitacao solicitacao = new Solicitacao();
        solicitacao.setProtocolo(gerarProtocoloUnico());
        solicitacao.setMorador(morador);
        solicitacao.setPontoId(ponto.getId());
        copiarEndereco(endereco, solicitacao);
        solicitacao.setSituacao(SituacaoSolicitacao.SOLICITADA);
        OffsetDateTime agora = agora();
        solicitacao.setDataCriacao(agora);
        solicitacao.setDataAtualizacao(agora);
        solicitacao.setObservacoes(requestDTO.getObservacoes());

        Solicitacao salva = solicitacaoRepository.saveAndFlush(solicitacao);
        List<ItemSolicitacao> itensSalvos = criarItens(salva, requestDTO.getItens(), aceitos);
        registrarHistorico(salva, null, SituacaoSolicitacao.SOLICITADA, "Sistema",
                "Abertura de solicitação de recolhimento", null);
        return converterParaResponseDTO(salva, itensSalvos);
    }

    // RN04: endereço e itens só podem ser corrigidos enquanto a solicitação estiver SOLICITADA.
    @Transactional
    public SolicitacaoResponseDTO atualizarAntesDoAgendamento(Long id, SolicitacaoRequestDTO requestDTO) {
        Solicitacao solicitacao = buscarEntidade(id);
        validarVersao(solicitacao, requestDTO.getVersao());
        exigirSituacao(solicitacao, SituacaoSolicitacao.SOLICITADA,
                "Endereço e itens só podem ser corrigidos antes do agendamento.");

        if (!Objects.equals(solicitacao.getMorador().getId(), requestDTO.getMoradorId())
                || !Objects.equals(solicitacao.getPontoId(), requestDTO.getPontoId())) {
            throw erro(HttpStatus.BAD_REQUEST, "A correção não pode alterar o morador nem o ponto da solicitação.");
        }

        Endereco endereco = enderecoRepository.findById(requestDTO.getEnderecoId())
                .orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Endereço não encontrado."));
        validarTitularidadeEndereco(endereco, solicitacao.getMorador());
        Map<Long, MaterialAceito> aceitos = validarItens(solicitacao.getPontoId(), requestDTO.getItens());

        List<ItemSolicitacao> itensAntigos = itemSolicitacaoRepository.findBySolicitacaoId(id);
        itemSolicitacaoRepository.deleteAll(itensAntigos);
        itemSolicitacaoRepository.flush();
        copiarEndereco(endereco, solicitacao);
        solicitacao.setObservacoes(requestDTO.getObservacoes());
        solicitacao.setDataAtualizacao(agora());
        Solicitacao atualizada = solicitacaoRepository.saveAndFlush(solicitacao);
        List<ItemSolicitacao> itensNovos = itemSolicitacaoRepository.saveAll(
                criarItensSemPersistir(atualizada, requestDTO.getItens(), aceitos));
        return converterParaResponseDTO(atualizada, itensNovos);
    }

    // RF09: consulta paginada; o protótipo ainda não possui autenticação para aplicar escopo por ator.
    @Transactional(readOnly = true)
    public Page<SolicitacaoResponseDTO> listarComFiltros(Long moradorId, Long pontoId,
                                                         SituacaoSolicitacao situacao,
                                                         OffsetDateTime inicio, OffsetDateTime fim,
                                                         Pageable pageable) {
        if (inicio != null && fim != null && inicio.isAfter(fim)) {
            throw erro(HttpStatus.BAD_REQUEST, "A data inicial não pode ser posterior à data final.");
        }
        if (moradorId != null && moradorId <= 0) {
            throw erro(HttpStatus.BAD_REQUEST, "moradorId deve ser positivo.");
        }
        if (pontoId != null && pontoId <= 0) {
            throw erro(HttpStatus.BAD_REQUEST, "pontoId deve ser positivo.");
        }
        if (pageable == null || pageable.getPageNumber() < 0
                || pageable.getPageSize() < 1 || pageable.getPageSize() > 100) {
            throw erro(HttpStatus.BAD_REQUEST, "A página deve ser não negativa e o tamanho deve estar entre 1 e 100.");
        }
        Pageable limitado = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                pageable.getSort().isSorted() ? pageable.getSort() : Sort.by(Sort.Direction.DESC, "dataCriacao"));
        return solicitacaoRepository.findComFiltros(moradorId, pontoId, situacao, inicio, fim, limitado)
                .map(s -> converterParaResponseDTO(s, itemSolicitacaoRepository.findBySolicitacaoId(s.getId())));
    }

    @Transactional(readOnly = true)
    public SolicitacaoResponseDTO buscarPorId(Long id) {
        Solicitacao solicitacao = buscarEntidade(id);
        return converterParaResponseDTO(solicitacao, itemSolicitacaoRepository.findBySolicitacaoId(id));
    }

    // RF10: aceita a solicitação e agenda data/hora futura.
    @Transactional
    public SolicitacaoResponseDTO agendar(Long id, AgendarSolicitacaoRequestDTO dados, String responsavel) {
        Solicitacao solicitacao = buscarEntidade(id);
        validarVersao(solicitacao, dados.getVersao());
        exigirSituacao(solicitacao, SituacaoSolicitacao.SOLICITADA,
                "Somente solicitações SOLICITADA podem ser agendadas.");
        validarDataFutura(dados.getDataAgendamento());
        SituacaoSolicitacao anterior = solicitacao.getSituacao();
        solicitacao.setDataAgendamento(dados.getDataAgendamento());
        solicitacao.setSituacao(SituacaoSolicitacao.AGENDADA);
        solicitacao.setDataAtualizacao(agora());
        Solicitacao atualizada = solicitacaoRepository.saveAndFlush(solicitacao);
        registrarHistorico(atualizada, anterior, SituacaoSolicitacao.AGENDADA, responsavel,
                "Solicitação aceita e agendada.", null);
        return respostaAtualizada(atualizada);
    }

    // RF11: recusa somente enquanto a solicitação estiver SOLICITADA.
    @Transactional
    public SolicitacaoResponseDTO recusar(Long id, RecusarSolicitacaoRequestDTO dados, String responsavel) {
        Solicitacao solicitacao = buscarEntidade(id);
        validarVersao(solicitacao, dados.getVersao());
        exigirSituacao(solicitacao, SituacaoSolicitacao.SOLICITADA,
                "Somente solicitações SOLICITADA podem ser recusadas.");
        SituacaoSolicitacao anterior = solicitacao.getSituacao();
        solicitacao.setSituacao(SituacaoSolicitacao.RECUSADA);
        solicitacao.setDataAtualizacao(agora());
        Solicitacao atualizada = solicitacaoRepository.saveAndFlush(solicitacao);
        registrarHistorico(atualizada, anterior, SituacaoSolicitacao.RECUSADA, responsavel, dados.getMotivo().strip(), null);
        return respostaAtualizada(atualizada);
    }

    // RF12: cancelamento permitido apenas antes do início da coleta.
    @Transactional
    public SolicitacaoResponseDTO cancelarSolicitacao(Long id, CancelarSolicitacaoRequestDTO dados, String responsavel) {
        Solicitacao solicitacao = buscarEntidade(id);
        validarVersao(solicitacao, dados.getVersao());
        if (solicitacao.getSituacao() != SituacaoSolicitacao.SOLICITADA
                && solicitacao.getSituacao() != SituacaoSolicitacao.AGENDADA) {
            throw erro(HttpStatus.CONFLICT, "Só é possível cancelar solicitações SOLICITADA ou AGENDADA.");
        }
        SituacaoSolicitacao anterior = solicitacao.getSituacao();
        solicitacao.setSituacao(SituacaoSolicitacao.CANCELADA);
        solicitacao.setDataAtualizacao(agora());
        Solicitacao atualizada = solicitacaoRepository.saveAndFlush(solicitacao);
        registrarHistorico(atualizada, anterior, SituacaoSolicitacao.CANCELADA, responsavel, dados.getMotivo().strip(), null);
        return respostaAtualizada(atualizada);
    }

    // RF13: início explícito da execução.
    @Transactional
    public SolicitacaoResponseDTO iniciar(Long id, VersaoRequestDTO dados, String responsavel) {
        Solicitacao solicitacao = buscarEntidade(id);
        validarVersao(solicitacao, dados.getVersao());
        exigirSituacao(solicitacao, SituacaoSolicitacao.AGENDADA,
                "Somente solicitações AGENDADA podem iniciar o recolhimento.");
        SituacaoSolicitacao anterior = solicitacao.getSituacao();
        solicitacao.setSituacao(SituacaoSolicitacao.EM_RECOLHIMENTO);
        solicitacao.setDataAtualizacao(agora());
        Solicitacao atualizada = solicitacaoRepository.saveAndFlush(solicitacao);
        registrarHistorico(atualizada, anterior, SituacaoSolicitacao.EM_RECOLHIMENTO, responsavel,
                "Recolhimento iniciado.", null);
        return respostaAtualizada(atualizada);
    }

    // RF13: conclusão apenas após início; todos os itens devem receber quantidade efetiva.
    @Transactional
    public SolicitacaoResponseDTO concluirSolicitacao(Long id, ConcluirSolicitacaoRequestDTO dados, String responsavel) {
        Solicitacao solicitacao = buscarEntidade(id);
        validarVersao(solicitacao, dados.getVersao());
        exigirSituacao(solicitacao, SituacaoSolicitacao.EM_RECOLHIMENTO,
                "O recolhimento precisa estar EM_RECOLHIMENTO para ser concluído.");

        List<ItemSolicitacao> itens = itemSolicitacaoRepository.findBySolicitacaoId(id);
        if (dados.getItens() == null || dados.getItens().size() != itens.size()) {
            throw erro(HttpStatus.BAD_REQUEST, "Informe a quantidade efetiva de todos os itens da solicitação, sem omissões.");
        }
        Map<Long, ItemSolicitacao> itensPorId = itens.stream()
                .collect(Collectors.toMap(ItemSolicitacao::getId, Function.identity()));
        Set<Long> itensInformados = new HashSet<>();
        boolean existeQuantidadePositiva = false;

        for (ItemConclusaoDTO itemDTO : dados.getItens()) {
            if (itemDTO.getItemId() == null || !itensInformados.add(itemDTO.getItemId())) {
                throw erro(HttpStatus.BAD_REQUEST, "Cada item da solicitação deve ser informado uma única vez.");
            }
            ItemSolicitacao item = itensPorId.get(itemDTO.getItemId());
            if (item == null) {
                throw erro(HttpStatus.BAD_REQUEST, "O item informado não pertence a esta solicitação.");
            }
            BigDecimal quantidade = itemDTO.getQuantidadeEfetiva();
            if (quantidade == null || quantidade.signum() < 0) {
                throw erro(HttpStatus.BAD_REQUEST, "A quantidade efetiva não pode ser nula ou negativa.");
            }
            validarQuantidadeInteiraQuandoPeca(item.getUnidade(), quantidade, "efetiva");
            item.setQuantidadeEfetiva(quantidade);
            existeQuantidadePositiva |= quantidade.signum() > 0;
        }
        if (!existeQuantidadePositiva) {
            throw erro(HttpStatus.CONFLICT, "A conclusão exige pelo menos uma quantidade efetiva maior que zero.");
        }
        itemSolicitacaoRepository.saveAll(itens);

        SituacaoSolicitacao anterior = solicitacao.getSituacao();
        solicitacao.setSituacao(SituacaoSolicitacao.CONCLUIDA);
        solicitacao.setDataAtualizacao(agora());
        if (dados.getObservacoes() != null && !dados.getObservacoes().isBlank()) {
            String observacaoAtual = solicitacao.getObservacoes() == null ? "" : solicitacao.getObservacoes().strip();
            String observacaoConclusao = "Conclusão: " + dados.getObservacoes().strip();
            String combinada = observacaoAtual.isBlank() ? observacaoConclusao : observacaoAtual + " | " + observacaoConclusao;
            solicitacao.setObservacoes(combinada.substring(0, Math.min(1000, combinada.length())));
        }
        Solicitacao atualizada = solicitacaoRepository.saveAndFlush(solicitacao);
        registrarHistorico(atualizada, anterior, SituacaoSolicitacao.CONCLUIDA, responsavel,
                "Recolhimento concluído.", null);
        return converterParaResponseDTO(atualizada, itens);
    }

    // RF14: tentativa sem sucesso em execução retorna a solicitação para AGENDADA.
    @Transactional
    public SolicitacaoResponseDTO registrarFrustracao(Long id, RegistrarFrustracaoRequestDTO dados, String responsavel) {
        Solicitacao solicitacao = buscarEntidade(id);
        validarVersao(solicitacao, dados.getVersao());
        exigirSituacao(solicitacao, SituacaoSolicitacao.EM_RECOLHIMENTO,
                "Só é possível registrar tentativa frustrada durante EM_RECOLHIMENTO.");
        validarDataFutura(dados.getNovaDataHora());
        SituacaoSolicitacao anterior = solicitacao.getSituacao();
        solicitacao.setSituacao(SituacaoSolicitacao.AGENDADA);
        solicitacao.setDataAgendamento(dados.getNovaDataHora());
        solicitacao.setDataAtualizacao(agora());
        Solicitacao atualizada = solicitacaoRepository.saveAndFlush(solicitacao);
        registrarHistorico(atualizada, anterior, SituacaoSolicitacao.AGENDADA, responsavel,
                dados.getMotivo().strip(), dados.getDadosTentativa());
        return respostaAtualizada(atualizada);
    }

    // RF14: reagendamento de uma solicitação que já estava AGENDADA.
    @Transactional
    public SolicitacaoResponseDTO reagendar(Long id, ReagendarSolicitacaoRequestDTO dados, String responsavel) {
        Solicitacao solicitacao = buscarEntidade(id);
        validarVersao(solicitacao, dados.getVersao());
        exigirSituacao(solicitacao, SituacaoSolicitacao.AGENDADA,
                "Somente solicitações AGENDADA podem ser reagendadas.");
        validarDataFutura(dados.getNovaDataHora());
        solicitacao.setDataAgendamento(dados.getNovaDataHora());
        solicitacao.setDataAtualizacao(agora());
        Solicitacao atualizada = solicitacaoRepository.saveAndFlush(solicitacao);
        registrarHistorico(atualizada, SituacaoSolicitacao.AGENDADA, SituacaoSolicitacao.AGENDADA,
                responsavel, dados.getMotivo().strip(), null);
        return respostaAtualizada(atualizada);
    }

    @Transactional(readOnly = true)
    public List<HistoricoResponseDTO> buscarHistorico(Long solicitacaoId, Pageable pageable) {
        if (!solicitacaoRepository.existsById(solicitacaoId)) {
            throw erro(HttpStatus.NOT_FOUND, "Solicitação não encontrada.");
        }
        return historicoRepository.findBySolicitacaoIdOrderByDataHoraAsc(solicitacaoId, pageable).stream()
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
                }).toList();
    }

    // RF15: totais de solicitações por situação e de quantidades efetivas por material/unidade.
    @Transactional(readOnly = true)
    public ResumoGerencialResponseDTO obterResumoGerencial(OffsetDateTime inicio, OffsetDateTime fim) {
        if (inicio == null || fim == null) {
            throw erro(HttpStatus.BAD_REQUEST, "As datas de início e fim são obrigatórias.");
        }
        if (inicio.isAfter(fim)) {
            throw erro(HttpStatus.BAD_REQUEST, "A data de início não pode ser posterior à data de fim.");
        }
        List<TotalPorStatusDTO> totaisStatus = solicitacaoRepository.contarPorStatusNoPeriodo(inicio, fim);
        Map<SituacaoSolicitacao, Long> contagens = new EnumMap<>(SituacaoSolicitacao.class);
        totaisStatus.forEach(total -> contagens.put(total.getSituacao(), total.getQuantidade()));
        List<TotalPorStatusDTO> todosStatus = new ArrayList<>();
        for (SituacaoSolicitacao status : SituacaoSolicitacao.values()) {
            todosStatus.add(new TotalPorStatusDTO(status, contagens.getOrDefault(status, 0L)));
        }
        List<TotalPorMaterialDTO> totaisMaterial = itemSolicitacaoRepository
                .somarQuantidadePorMaterialNoPeriodo(inicio, fim);
        return new ResumoGerencialResponseDTO(inicio, fim, todosStatus, totaisMaterial);
    }

    private Map<Long, MaterialAceito> validarItens(Long pontoId, List<ItemSolicitacaoRequestDTO> itens) {
        if (itens == null || itens.isEmpty()) {
            throw erro(HttpStatus.BAD_REQUEST, "A solicitação deve conter pelo menos um item.");
        }
        Map<Long, MaterialAceito> aceitos = materialAceitoRepository.listarMateriaisAtivosDoPonto(pontoId)
                .stream().collect(Collectors.toMap(MaterialAceito::getMaterialId, Function.identity()));
        Set<Long> idsInformados = new HashSet<>();
        for (ItemSolicitacaoRequestDTO item : itens) {
            if (item.getMaterialId() == null || !idsInformados.add(item.getMaterialId())) {
                throw erro(HttpStatus.CONFLICT, "Cada material deve aparecer uma única vez na solicitação.");
            }
            MaterialAceito aceito = aceitos.get(item.getMaterialId());
            if (aceito == null) {
                throw erro(HttpStatus.CONFLICT, "O material " + item.getMaterialId() + " não está ativo ou não é aceito pelo ponto.");
            }
            if (item.getUnidade() == null || !aceito.getUnidadeMedida().equalsIgnoreCase(item.getUnidade().strip())) {
                throw erro(HttpStatus.CONFLICT, "A unidade do material " + item.getMaterialId()
                        + " deve ser " + aceito.getUnidadeMedida() + ".");
            }
            BigDecimal quantidade = item.getQuantidadeEstimada();
            if (quantidade == null || quantidade.signum() <= 0) {
                throw erro(HttpStatus.BAD_REQUEST, "A quantidade estimada deve ser maior que zero.");
            }
            validarQuantidadeInteiraQuandoPeca(aceito.getUnidadeMedida(), quantidade, "estimada");
            BigDecimal limite = aceito.getLimitePorSolicitacao();
            if (limite != null && quantidade.compareTo(limite) > 0) {
                throw erro(HttpStatus.CONFLICT, "A quantidade do material " + item.getMaterialId()
                        + " excede o limite por solicitação (" + limite + ").");
            }
        }
        return aceitos;
    }

    private List<ItemSolicitacao> criarItens(Solicitacao solicitacao, List<ItemSolicitacaoRequestDTO> dados,
                                              Map<Long, MaterialAceito> aceitos) {
        return itemSolicitacaoRepository.saveAll(criarItensSemPersistir(solicitacao, dados, aceitos));
    }

    private List<ItemSolicitacao> criarItensSemPersistir(Solicitacao solicitacao, List<ItemSolicitacaoRequestDTO> dados,
                                                          Map<Long, MaterialAceito> aceitos) {
        return dados.stream().map(dto -> {
            ItemSolicitacao item = new ItemSolicitacao();
            item.setSolicitacao(solicitacao);
            item.setMaterialId(dto.getMaterialId());
            item.setUnidade(aceitos.get(dto.getMaterialId()).getUnidadeMedida());
            item.setQuantidadeEstimada(dto.getQuantidadeEstimada());
            return item;
        }).toList();
    }

    private void validarTitularidadeEndereco(Endereco endereco, Morador morador) {
        if (!endereco.getMoradorId().equals(morador.getId())) {
            throw erro(HttpStatus.BAD_REQUEST, "O endereço informado não pertence ao morador.");
        }
    }

    private PontoColeta buscarPontoCompativel(Long pontoId) {
        PontoColeta ponto = pontoColetaRepository.findById(pontoId)
                .orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Ponto de coleta não encontrado."));
        if (!ponto.isAtivo()) {
            throw erro(HttpStatus.CONFLICT, "Não é possível solicitar coleta em um ponto inativo.");
        }
        if (!ponto.isRecolhimentoDomiciliar()) {
            throw erro(HttpStatus.CONFLICT, "Este ponto não realiza recolhimento domiciliar.");
        }
        return ponto;
    }

    private void copiarEndereco(Endereco endereco, Solicitacao solicitacao) {
        solicitacao.setLogradouro(endereco.getLogradouro());
        solicitacao.setNumero(endereco.getNumero());
        solicitacao.setSemNumero(endereco.isSemNumero());
        solicitacao.setBairro(endereco.getBairro());
        solicitacao.setMunicipio(endereco.getMunicipio());
        solicitacao.setUf(endereco.getUf());
        solicitacao.setCep(endereco.getCep());
        solicitacao.setComplemento(endereco.getComplemento());
    }

    private Solicitacao buscarEntidade(Long id) {
        return solicitacaoRepository.findById(id)
                .orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Solicitação não encontrada com o ID: " + id));
    }

    private void validarVersao(Solicitacao solicitacao, Long versaoEsperada) {
        if (versaoEsperada == null) {
            throw erro(HttpStatus.BAD_REQUEST, "Informe a versão atual da solicitação.");
        }
        if (!Objects.equals(solicitacao.getVersao(), versaoEsperada)) {
            throw erro(HttpStatus.CONFLICT, "A solicitação foi alterada por outra operação. Consulte novamente antes de atualizar.");
        }
    }

    private void exigirSituacao(Solicitacao solicitacao, SituacaoSolicitacao esperada, String mensagem) {
        if (solicitacao.getSituacao() != esperada) {
            throw erro(HttpStatus.CONFLICT, mensagem);
        }
    }

    private void validarDataFutura(OffsetDateTime data) {
        if (data == null || !data.isAfter(agora())) {
            throw erro(HttpStatus.BAD_REQUEST, "A data e hora informadas devem ser futuras e conter offset de fuso horário.");
        }
    }

    private void validarQuantidadeInteiraQuandoPeca(String unidade, BigDecimal quantidade, String tipo) {
        String normalizada = Normalizer.normalize(unidade == null ? "" : unidade, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
        boolean unidadeDePeca = Set.of("un", "und", "unid", "unidade", "unidades", "peca", "pecas", "pc", "pcs")
                .contains(normalizada);
        if (unidadeDePeca && quantidade.stripTrailingZeros().scale() > 0) {
            throw erro(HttpStatus.BAD_REQUEST, "A quantidade " + tipo + " para materiais contados por peça deve ser inteira.");
        }
    }

    private String gerarProtocoloUnico() {
        String protocolo;
        do {
            protocolo = "REC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        } while (solicitacaoRepository.existsByProtocolo(protocolo));
        return protocolo;
    }

    private void registrarHistorico(Solicitacao solicitacao, SituacaoSolicitacao anterior,
                                    SituacaoSolicitacao nova, String responsavel, String motivo, String dadosTentativa) {
        Historico historico = new Historico();
        historico.setSolicitacao(solicitacao);
        historico.setSituacaoAnterior(anterior);
        historico.setSituacaoNova(nova);
        historico.setDataHora(agora());
        historico.setResponsavel(limitar(responsavel == null || responsavel.isBlank() ? "Sistema" : responsavel.strip(), 255));
        historico.setMotivo(limitar(motivo, 255));
        historico.setDadosTentativa(limitar(dadosTentativa, 255));
        historicoRepository.save(historico);
    }

    private SolicitacaoResponseDTO respostaAtualizada(Solicitacao solicitacao) {
        return converterParaResponseDTO(solicitacao, itemSolicitacaoRepository.findBySolicitacaoId(solicitacao.getId()));
    }

    private SolicitacaoResponseDTO converterParaResponseDTO(Solicitacao solicitacao, List<ItemSolicitacao> itens) {
        SolicitacaoResponseDTO dto = new SolicitacaoResponseDTO();
        dto.setId(solicitacao.getId());
        dto.setProtocolo(solicitacao.getProtocolo());
        dto.setMoradorId(solicitacao.getMorador() == null ? null : solicitacao.getMorador().getId());
        dto.setPontoId(solicitacao.getPontoId());
        dto.setLogradouro(solicitacao.getLogradouro());
        dto.setNumero(solicitacao.getNumero());
        dto.setSemNumero(solicitacao.isSemNumero());
        dto.setBairro(solicitacao.getBairro());
        dto.setMunicipio(solicitacao.getMunicipio());
        dto.setUf(solicitacao.getUf());
        dto.setCep(solicitacao.getCep());
        dto.setComplemento(solicitacao.getComplemento());
        dto.setSituacao(solicitacao.getSituacao());
        dto.setDataCriacao(solicitacao.getDataCriacao());
        dto.setDataAgendamento(solicitacao.getDataAgendamento());
        dto.setDataAtualizacao(solicitacao.getDataAtualizacao());
        dto.setVersao(solicitacao.getVersao());
        dto.setObservacoes(solicitacao.getObservacoes());
        if (itens != null) {
            dto.setItens(itens.stream().map(item -> {
                ItemSolicitacaoResponseDTO itemDTO = new ItemSolicitacaoResponseDTO();
                itemDTO.setId(item.getId());
                itemDTO.setMaterialId(item.getMaterialId());
                itemDTO.setUnidade(item.getUnidade());
                itemDTO.setQuantidadeEstimada(item.getQuantidadeEstimada());
                itemDTO.setQuantidadeEfetiva(item.getQuantidadeEfetiva());
                return itemDTO;
            }).toList());
        }
        return dto;
    }

    private String limitar(String texto, int maximo) {
        if (texto == null || texto.length() <= maximo) {
            return texto;
        }
        return texto.substring(0, maximo);
    }

    private OffsetDateTime agora() {
        return OffsetDateTime.now(FUSO_DO_PROJETO);
    }

    private ResponseStatusException erro(HttpStatus status, String mensagem) {
        return new ResponseStatusException(status, mensagem);
    }
}
