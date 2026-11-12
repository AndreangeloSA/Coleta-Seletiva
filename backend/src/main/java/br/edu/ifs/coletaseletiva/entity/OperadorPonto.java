package br.edu.ifs.coletaseletiva.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** Entidade associativa do relacionamento N:M entre operador e ponto de coleta. */
@Entity
@Table(name = "operador_ponto", uniqueConstraints =
        @UniqueConstraint(name = "uk_operador_ponto", columnNames = {"operador_id", "ponto_id"}))
public class OperadorPonto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "operador_id", nullable = false)
    private Operador operador;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ponto_id", nullable = false)
    private PontoColeta ponto;

    protected OperadorPonto() {
    }

    public OperadorPonto(Operador operador, PontoColeta ponto) {
        this.operador = operador;
        this.ponto = ponto;
    }

    public Long getId() {
        return id;
    }

    public Long getOperadorId() {
        return operador.getId();
    }

    public Long getPontoId() {
        return ponto.getId();
    }

    public String getNomePonto() {
        return ponto.getNome();
    }
}
