package br.edu.ifs.coletaseletiva.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record ErroApiDTO(
        OffsetDateTime dataHora,
        int status,
        String codigo,
        String mensagem,
        List<ErroCampoDTO> erros) {
}
