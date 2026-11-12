package br.edu.ifs.coletaseletiva.entity;

import br.edu.ifs.coletaseletiva.dto.OperadorRequestDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Locale;

@Entity
@Table(name = "operador", uniqueConstraints =
        @UniqueConstraint(name = "uk_operador_email", columnNames = "email"))
public class Operador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String nome;

    @Column(nullable = false, length = 255)
    private String email;

    protected Operador() {
    }

    public Operador(OperadorRequestDTO dados) {
        atualizar(dados);
    }

    public void atualizar(OperadorRequestDTO dados) {
        this.nome = dados.nome().strip();
        this.email = dados.email().strip().toLowerCase(Locale.ROOT);
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }
}
