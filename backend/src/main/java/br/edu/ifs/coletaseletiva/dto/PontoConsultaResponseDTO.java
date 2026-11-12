package br.edu.ifs.coletaseletiva.dto;

import br.edu.ifs.coletaseletiva.entity.PontoColeta;
import java.util.List;

/** Resposta usada na consulta pública paginada de pontos (RF06). */
public record PontoConsultaResponseDTO(
        Long id,
        String nome,
        String endereco,
        String municipio,
        String bairro,
        String contato,
        String horariosFuncionamento,
        boolean ativo,
        boolean recolhimentoDomiciliar,
        List<MaterialAceitoConsultaDTO> materiaisAceitos
) {
    public PontoConsultaResponseDTO(PontoColeta ponto, List<MaterialAceitoConsultaDTO> materiaisAceitos) {
        this(
                ponto.getId(),
                ponto.getNome(),
                ponto.getEndereco(),
                ponto.getMunicipio(),
                ponto.getBairro(),
                ponto.getContato(),
                ponto.getHorariosFuncionamento(),
                ponto.isAtivo(),
                ponto.isRecolhimentoDomiciliar(),
                List.copyOf(materiaisAceitos)
        );
    }
}
