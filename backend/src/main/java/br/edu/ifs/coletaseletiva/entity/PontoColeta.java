package br.edu.ifs.coletaseletiva.entity;
import jakarta.persistence.*;
@Entity @Table(name="ponto_coleta")
public class PontoColeta {

 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false) private String nome;
 @Column(nullable=false, length = 500) private String endereco;
 private String contato;
 private String municipio;
 private String bairro;
 public String getMunicipio(){return municipio;}
 public String getBairro(){return bairro;}
 public void atualizarLocalidade(String municipio,String bairro){
     this.municipio=municipio;
     this.bairro=bairro;
 }

 @Column(name="horarios_funcionamento", length = 500) private String horariosFuncionamento;
 @Column(nullable=false) private boolean ativo=true;
 @Column(name="recolhimento_domiciliar",nullable=false) private boolean recolhimentoDomiciliar;
 public PontoColeta(){}

 public void atualizar(String n,String e,String c,String h,Boolean a,boolean r){nome=n.strip();endereco=e.strip();contato=c;horariosFuncionamento=h;if(a!=null)ativo=a;recolhimentoDomiciliar=r;}

 public void inativar(){ativo=false;}

 public Long getId(){return id;} public String getNome(){return nome;} public String getEndereco(){return endereco;} public String getContato(){return contato;} public String getHorariosFuncionamento(){return horariosFuncionamento;} public boolean isAtivo(){return ativo;} public boolean isRecolhimentoDomiciliar(){return recolhimentoDomiciliar;}
}

