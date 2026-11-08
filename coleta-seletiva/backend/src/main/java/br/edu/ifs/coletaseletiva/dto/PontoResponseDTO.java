package br.edu.ifs.coletaseletiva.dto;
import br.edu.ifs.coletaseletiva.entity.PontoColeta;
public record PontoResponseDTO(Long id,String nome,String endereco,String contato,String horariosFuncionamento,boolean ativo,boolean recolhimentoDomiciliar){public PontoResponseDTO(PontoColeta p){this(p.getId(),p.getNome(),p.getEndereco(),p.getContato(),p.getHorariosFuncionamento(),p.isAtivo(),p.isRecolhimentoDomiciliar());}}
