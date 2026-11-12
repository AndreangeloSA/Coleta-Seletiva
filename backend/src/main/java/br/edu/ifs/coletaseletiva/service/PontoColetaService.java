package br.edu.ifs.coletaseletiva.service;

import br.edu.ifs.coletaseletiva.dto.MaterialAceitoConsultaDTO;
import br.edu.ifs.coletaseletiva.dto.PontoConsultaResponseDTO;
import br.edu.ifs.coletaseletiva.dto.PontoRequestDTO;
import br.edu.ifs.coletaseletiva.entity.MaterialAceito;
import br.edu.ifs.coletaseletiva.entity.PontoColeta;
import br.edu.ifs.coletaseletiva.repository.MaterialAceitoRepository;
import br.edu.ifs.coletaseletiva.repository.PontoColetaRepository;
import br.edu.ifs.coletaseletiva.repository.SolicitacaoRepository;
import br.edu.ifs.coletaseletiva.enums.SituacaoSolicitacao;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class PontoColetaService {
    private final PontoColetaRepository pontos;
    private final MaterialAceitoRepository materiaisAceitos;
    private final SolicitacaoRepository solicitacoes;

    public PontoColetaService(PontoColetaRepository pontos, MaterialAceitoRepository materiaisAceitos,
                              SolicitacaoRepository solicitacoes) {
        this.pontos = pontos;
        this.materiaisAceitos = materiaisAceitos;
        this.solicitacoes = solicitacoes;
    }

    public PontoColeta cadastrar(PontoRequestDTO dados) {
        return salvar(new PontoColeta(), dados);
    }

    public PontoColeta atualizar(Long id, PontoRequestDTO dados) {
        PontoColeta ponto = buscar(id);
        if (ponto.isAtivo() && Boolean.FALSE.equals(dados.ativo())) {
            validarSemSolicitacoesAbertas(id);
        }
        return salvar(ponto, dados);
    }

    @Transactional(readOnly = true)
    public List<PontoColeta> listarAtivos() {
        return pontos.findAll().stream().filter(PontoColeta::isAtivo).toList();
    }

    /** Pesquisa pública do RF06, sempre limitada a pontos ativos e com paginação. */
    @Transactional(readOnly = true)
    public Page<PontoConsultaResponseDTO> pesquisarAtivos(String nome, String municipio, String bairro,
                                                           Long materialId, String material,
                                                           int pagina, int tamanho) {
        if (pagina < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A página deve ser maior ou igual a zero");
        }
        if (tamanho < 1 || tamanho > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O tamanho da página deve estar entre 1 e 100");
        }
        if (materialId != null && materialId < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "materialId deve ser positivo");
        }

        Pageable pageable = PageRequest.of(pagina, tamanho, Sort.by(Sort.Direction.ASC, "nome"));
        Page<PontoColeta> resultado = pontos.pesquisarAtivos(
                normalizarFiltro(nome), normalizarFiltro(municipio), normalizarFiltro(bairro),
                materialId, normalizarFiltro(material), pageable);

        List<Long> ids = resultado.getContent().stream().map(PontoColeta::getId).toList();
        if (ids.isEmpty()) {
            return resultado.map(ponto -> new PontoConsultaResponseDTO(ponto, List.of()));
        }

        // O agrupamento é montado abaixo com os vínculos carregados em lote, sem consultar por ponto.
        List<MaterialAceito> vinculos = materiaisAceitos
                .listarMateriaisAtivosDosPontos(ids);
        Map<Long, List<MaterialAceitoConsultaDTO>> materiaisPorPonto = vinculos.stream()
                .collect(Collectors.groupingBy(
                        MaterialAceito::getPontoId,
                        Collectors.mapping(MaterialAceitoConsultaDTO::new, Collectors.toList())
                ));

        return resultado.map(ponto -> new PontoConsultaResponseDTO(
                ponto, materiaisPorPonto.getOrDefault(ponto.getId(), List.of())));
    }

    @Transactional(readOnly = true)
    public PontoConsultaResponseDTO consultarComMateriais(Long id) {
        PontoColeta ponto = buscar(id);
        List<MaterialAceitoConsultaDTO> aceitos = materiaisAceitos
                .listarMateriaisAtivosDoPonto(id)
                .stream().map(MaterialAceitoConsultaDTO::new).toList();
        return new PontoConsultaResponseDTO(ponto, aceitos);
    }

    @Transactional(readOnly = true)
    public PontoColeta buscar(Long id) {
        return pontos.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Ponto não encontrado"));
    }

    public PontoColeta inativar(Long id) {
        PontoColeta ponto = buscar(id);
        validarSemSolicitacoesAbertas(id);
        ponto.inativar();
        return ponto;
    }

    private void validarSemSolicitacoesAbertas(Long pontoId) {
        List<SituacaoSolicitacao> abertas = List.of(SituacaoSolicitacao.SOLICITADA,
                SituacaoSolicitacao.AGENDADA, SituacaoSolicitacao.EM_RECOLHIMENTO);
        if (solicitacoes.existsByPontoIdAndSituacaoIn(pontoId, abertas)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "O ponto não pode ser inativado enquanto possuir solicitações abertas. Resolva ou cancele os atendimentos primeiro.");
        }
    }

    private PontoColeta salvar(PontoColeta ponto, PontoRequestDTO dados) {
        ponto.atualizar(dados.nome(), dados.endereco(), dados.contato(),
                dados.horariosFuncionamento(), dados.ativo(), dados.recolhimentoDomiciliar());
        ponto.atualizarLocalidade(dados.municipio(), dados.bairro());
        return pontos.save(ponto);
    }

    private String normalizarFiltro(String filtro) {
        if (filtro == null || filtro.isBlank()) {
            return null;
        }
        return filtro.strip();
    }
}
