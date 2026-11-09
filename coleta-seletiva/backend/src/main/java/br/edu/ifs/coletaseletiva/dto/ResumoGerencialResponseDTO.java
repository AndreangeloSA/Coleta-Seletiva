package br.edu.ifs.coletaseletiva.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ResumoGerencialResponseDTO {

    private LocalDateTime inicio;
    private LocalDateTime fim;
    private List<TotalPorStatusDTO> totaisPorStatus;
    private List<TotalPorMaterialDTO> totaisPorMaterial;

    public ResumoGerencialResponseDTO() {
    }

    public ResumoGerencialResponseDTO(LocalDateTime inicio, LocalDateTime fim, List<TotalPorStatusDTO> totaisPorStatus, List<TotalPorMaterialDTO> totaisPorMaterial) {
        this.inicio = inicio;
        this.fim = fim;
        this.totaisPorStatus = totaisPorStatus;
        this.totaisPorMaterial = totaisPorMaterial;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public void setInicio(LocalDateTime inicio) {
        this.inicio = inicio;
    }

    public LocalDateTime getFim() {
        return fim;
    }

    public void setFim(LocalDateTime fim) {
        this.fim = fim;
    }

    public List<TotalPorStatusDTO> getTotaisPorStatus() {
        return totaisPorStatus;
    }

    public void setTotaisPorStatus(List<TotalPorStatusDTO> totaisPorStatus) {
        this.totaisPorStatus = totaisPorStatus;
    }

    public List<TotalPorMaterialDTO> getTotaisPorMaterial() {
        return totaisPorMaterial;
    }

    public void setTotaisPorMaterial(List<TotalPorMaterialDTO> totaisPorMaterial) {
        this.totaisPorMaterial = totaisPorMaterial;
    }
}
