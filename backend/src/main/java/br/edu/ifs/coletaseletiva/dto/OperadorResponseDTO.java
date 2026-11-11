package br.edu.ifs.coletaseletiva.dto;

import br.edu.ifs.coletaseletiva.entity.Operador;

public record OperadorResponseDTO(Long id, String nome, String email) {
    public OperadorResponseDTO(Operador operador) {
        this(operador.getId(), operador.getNome(), operador.getEmail());
    }
}
