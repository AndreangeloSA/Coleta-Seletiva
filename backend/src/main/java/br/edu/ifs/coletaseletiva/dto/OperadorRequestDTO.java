package br.edu.ifs.coletaseletiva.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Locale;

public record OperadorRequestDTO(
        @NotBlank @Size(max = 255) String nome,
        @NotBlank @Email @Size(max = 255) String email
) {
    public OperadorRequestDTO {
        if (nome != null) {
            nome = nome.strip();
        }
        if (email != null) {
            email = email.strip().toLowerCase(Locale.ROOT);
        }
    }
}
