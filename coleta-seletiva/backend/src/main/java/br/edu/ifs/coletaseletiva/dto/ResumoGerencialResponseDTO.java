package br.edu.ifs.coletaseletiva.dto;

import java.time.OffsetDateTime;
import java.util.List;

public class ResumoGerencialResponseDTO {

    private OffsetDateTime inicio;
    private OffsetDateTime fim;
    private List<TotalPorStatusDTO> totaisPorStatus;
    private List<TotalPorMaterialDTO> totaisPorMaterial;

    public ResumoGerencialResponseDTO() {
    }

    public ResumoGerencialResponseDTO(OffsetDateTime inicio, OffsetDateTime fim, List<TotalPorStatusDTO> totaisPorStatus, List<TotalPorMaterialDTO> totaisPorMaterial) {
        this.inicio = inicio;
        this.fim = fim;
        this.totaisPorStatus = totaisPorStatus;
        this.totaisPorMaterial = totaisPorMaterial;
    }

    public OffsetDateTime getInicio() {
        return inicio;
    }

    public void setInicio(OffsetDateTime inicio) {
        this.inicio = inicio;
    }

    public OffsetDateTime getFim() {
        return fim;
    }

    public void setFim(OffsetDateTime fim) {
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
