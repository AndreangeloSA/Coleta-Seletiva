package br.edu.ifs.coletaseletiva.service;

import br.edu.ifs.coletaseletiva.dto.MaterialAceitoRequestDTO;
import br.edu.ifs.coletaseletiva.entity.MaterialAceito;
import br.edu.ifs.coletaseletiva.repository.MaterialAceitoRepository;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class MaterialAceitoService {
    private final MaterialAceitoRepository vinculos;
    private final PontoColetaService pontos;
    private final MaterialService materiais;

    public MaterialAceitoService(MaterialAceitoRepository vinculos,
                                 PontoColetaService pontos,
                                 MaterialService materiais) {
        this.vinculos = vinculos;
        this.pontos = pontos;
        this.materiais = materiais;
    }

    public MaterialAceito vincular(Long pontoId, Long materialId, MaterialAceitoRequestDTO dados) {
        var ponto = pontos.buscar(pontoId);
        var material = materiais.buscar(materialId);
        if (!ponto.isAtivo() || !material.isAtivo()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ponto e material precisam estar ativos");
        }
        if (vinculos.existsByPonto_IdAndMaterial_Id(pontoId, materialId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Vínculo já existe");
        }
        try {
            return vinculos.saveAndFlush(new MaterialAceito(
                    ponto, material, dados.observacoes(), dados.limitePorSolicitacao()));
        } catch (DataIntegrityViolationException erro) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Vínculo já existe ou limite inválido", erro);
        }
    }

    @Transactional(readOnly = true)
    public List<MaterialAceito> listarDoPonto(Long pontoId) {
        pontos.buscar(pontoId);
        return vinculos.findByPonto_Id(pontoId);
    }

    public void remover(Long pontoId, Long materialId) {
        MaterialAceito vinculo = vinculos.findByPonto_Id(pontoId).stream()
                .filter(item -> item.getMaterialId().equals(materialId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vínculo não encontrado"));
        vinculos.delete(vinculo);
    }
}
