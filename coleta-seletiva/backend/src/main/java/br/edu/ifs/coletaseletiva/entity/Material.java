package br.edu.ifs.coletaseletiva.entity;
import jakarta.persistence.*;

@Entity @Table(name="material", uniqueConstraints=@UniqueConstraint(name="uk_material_nome",columnNames="nome"))
public class Material {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true) private String nome;

 private String descricao;

 @Column(name="unidade_medida",nullable=false) private String unidadeMedida;
 @Column(name="orientacao_preparo") private String orientacaoPreparo;
 @Column(nullable=false) private boolean ativo=true;
 public Material(){}

 public void atualizar(String n,String d,String u,String o){nome=n.strip();descricao=d;unidadeMedida=u.strip();orientacaoPreparo=o;}

 public void inativar(){ativo=false;}

 public Long getId(){return id;} public String getNome(){return nome;} public String getDescricao(){return descricao;} public String getUnidadeMedida(){return unidadeMedida;} public String getOrientacaoPreparo(){return orientacaoPreparo;} public boolean isAtivo(){return ativo;}
}

