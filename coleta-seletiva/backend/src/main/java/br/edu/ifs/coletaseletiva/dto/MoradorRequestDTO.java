package br.edu.ifs.coletaseletiva.dto;

import jakarta.validation.constraints.*;
import java.util.Locale;

public class MoradorRequestDTO {
    @NotBlank @Size(max = 255)
    private String nome;

    @NotBlank @Email @Size(max = 255)
    private String email;

    @Size(max = 255)
    private String telefone;

    public MoradorRequestDTO() {}

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome == null ? null : nome.strip(); }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email == null ? null : email.strip().toLowerCase(Locale.ROOT); }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone == null ? null : telefone.strip(); }
}