package br.edu.ifs.coletaseletiva.service;

import br.edu.ifs.coletaseletiva.dto.MaterialRequestDTO;
import br.edu.ifs.coletaseletiva.entity.Material;
import br.edu.ifs.coletaseletiva.repository.MaterialRepository;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class MaterialService {
    private final MaterialRepository materiais;

    public MaterialService(MaterialRepository materiais) {
        this.materiais = materiais;
    }

    public Material cadastrar(MaterialRequestDTO dados) {
        return salvar(new Material(), dados);
    }

    public Material atualizar(Long id, MaterialRequestDTO dados) {
        return salvar(buscar(id), dados);
    }

    @Transactional(readOnly = true)
    public List<Material> listar() {
        return materiais.findAll();
    }

    @Transactional(readOnly = true)
    public Material buscar(Long id) {
        return materiais.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Material não encontrado"));
    }

    public Material inativar(Long id) {
        Material material = buscar(id);
        material.inativar();
        return material;
    }

    private Material salvar(Material material, MaterialRequestDTO dados) {
        material.atualizar(dados.nome(), dados.descricao(), dados.unidadeMedida(), dados.orientacaoPreparo());
        try {
            return materiais.saveAndFlush(material);
        } catch (DataIntegrityViolationException erro) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nome de material já cadastrado", erro);
        }
    }
}
