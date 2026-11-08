package br.edu.ifs.coletaseletiva.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Locale;

public record DadosMorador(
        @NotBlank @Size(max = 255) String nome,
        @NotBlank @Email @Size(max = 255) String email,
        @Size(max = 255) String telefone) {
    public DadosMorador {
        nome = nome == null ? null : nome.strip();
        email = email == null ? null : email.strip().toLowerCase(Locale.ROOT);
        telefone = telefone == null ? null : telefone.strip();
    }
}

