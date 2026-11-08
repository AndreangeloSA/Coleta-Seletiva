package br.edu.ifs.coletaseletiva.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity @Table(name="material_aceito",uniqueConstraints=@UniqueConstraint(name="uk_ponto_material",columnNames={"ponto_id","material_id"}))
public class MaterialAceito {

 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false) @JoinColumn(name="ponto_id",nullable=false) private PontoColeta ponto;
 @ManyToOne(optional=false) @JoinColumn(name="material_id",nullable=false) private Material material;
 private String observacoes;

 @Column(name="limite_por_solicitacao",precision=12,scale=3) private BigDecimal limitePorSolicitacao;
 protected MaterialAceito(){}

 public MaterialAceito(PontoColeta p,Material m,String o,BigDecimal l){ponto=p;material=m;observacoes=o;limitePorSolicitacao=l;}

 public void atualizar(String o,BigDecimal l){observacoes=o;limitePorSolicitacao=l;}

 public Long getId(){return id;} public Long getPontoId(){return ponto.getId();} public Long getMaterialId(){return material.getId();} public String getNomeMaterial(){return material.getNome();} public String getUnidadeMedida(){return material.getUnidadeMedida();} public String getObservacoes(){return observacoes;} public BigDecimal getLimitePorSolicitacao(){return limitePorSolicitacao;}
}
