package br.edu.ifs.coletaseletiva.entity;

import br.edu.ifs.coletaseletiva.dto.DadosEndereco;
import jakarta.persistence.*;

@Entity
public class Endereco {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "morador_id", nullable = false, updatable = false)
    private Morador morador;

    @Column(nullable = false)
    private String logradouro;

    @Column(nullable = true)
    private String numero;

    @Column(nullable = false)
    private String bairro;

    @Column(nullable = false)
    private String municipio;

    @Column(nullable = false, length = 2)
    private String uf;

    @Column(nullable = false, length = 9)
    private String cep;

    @Column(nullable = true)
    private String complemento;

    @Column(nullable = false)
    private boolean semNumero;

    protected Endereco() {}

    public Endereco(Morador morador, DadosEndereco dados) {
        this.morador = morador;
        atualizar(dados);
    }

    public void atualizar(DadosEndereco dados) {
        logradouro = dados.logradouro();
        numero = dados.numero();
        bairro = dados.bairro();
        municipio = dados.municipio();
        uf = dados.uf();
        cep = dados.cep();
        complemento = dados.complemento();
        semNumero = dados.semNumero();
    }

    public Long getId() { return id; }
    public Long getMoradorId() { return morador.getId(); }
    public boolean isSemNumero() { return semNumero; }
    public String getLogradouro() { return logradouro; }
    public String getNumero() { return numero; }
    public String getBairro() { return bairro; }
    public String getMunicipio() { return municipio; }
    public String getUf() { return uf; }
    public String getCep() { return cep; }
    public String getComplemento() { return complemento; }
}
