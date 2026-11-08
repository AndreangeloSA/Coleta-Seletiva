package br.edu.ifs.coletaseletiva.entity;

import br.edu.ifs.coletaseletiva.dto.DadosMorador;
import jakarta.persistence.*;

@Entity
@Table(name = "morador", uniqueConstraints = @UniqueConstraint(name = "uk_morador_email", columnNames = "email"))
public class Morador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;
    @Column(nullable = false)
    private String email;
    private String telefone;

    public void atualizar(DadosMorador dados) {
        nome = dados.nome();
        email = dados.email();
        telefone = dados.telefone();
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getTelefone() { return telefone; }
}

