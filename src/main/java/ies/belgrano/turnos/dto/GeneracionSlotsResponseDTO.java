package ies.belgrano.turnos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "DTO de respuesta tras la generación masiva de slots de agenda médica hospitalaria")
public class GeneracionSlotsResponseDTO {

    @Schema(description = "Cantidad total de slots generados exitosamente", example = "16")
    private int totalSlotsGenerados;

    @Schema(description = "Identificador único del profesional médico", example = "1")
    private Long profesionalId;

    @Schema(description = "Nombre completo del profesional médico", example = "Dr. Roberto Pérez")
    private String profesionalNombreCompleto;

    @Schema(description = "Identificador único de la especialidad", example = "1")
    private Long especialidadId;

    @Schema(description = "Nombre de la especialidad médica", example = "Cardiología")
    private String especialidadNombre;

    @Schema(description = "Identificador único de la sede", example = "1")
    private Long sedeId;

    @Schema(description = "Nombre de la sede hospitalaria", example = "Hospital Central")
    private String sedeNombre;

    @Schema(description = "Fecha inicial del rango generado", example = "2026-10-20")
    private LocalDate fechaDesde;

    @Schema(description = "Fecha final del rango generado", example = "2026-10-25")
    private LocalDate fechaHasta;

    @Schema(description = "Listado de slots/turnos creados en estado DISPONIBLE")
    private List<TurnoResponseDTO> slots;

    public GeneracionSlotsResponseDTO() {
    }

    public GeneracionSlotsResponseDTO(int totalSlotsGenerados, Long profesionalId, String profesionalNombreCompleto,
                                      Long especialidadId, String especialidadNombre, Long sedeId, String sedeNombre,
                                      LocalDate fechaDesde, LocalDate fechaHasta, List<TurnoResponseDTO> slots) {
        this.totalSlotsGenerados = totalSlotsGenerados;
        this.profesionalId = profesionalId;
        this.profesionalNombreCompleto = profesionalNombreCompleto;
        this.especialidadId = especialidadId;
        this.especialidadNombre = especialidadNombre;
        this.sedeId = sedeId;
        this.sedeNombre = sedeNombre;
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
        this.slots = slots;
    }

    public int getTotalSlotsGenerados() {
        return totalSlotsGenerados;
    }

    public int getCantidadSlotsGenerados() {
        return totalSlotsGenerados;
    }

    public void setTotalSlotsGenerados(int totalSlotsGenerados) {
        this.totalSlotsGenerados = totalSlotsGenerados;
    }

    public Long getProfesionalId() {
        return profesionalId;
    }

    public void setProfesionalId(Long profesionalId) {
        this.profesionalId = profesionalId;
    }

    public String getProfesionalNombreCompleto() {
        return profesionalNombreCompleto;
    }

    public void setProfesionalNombreCompleto(String profesionalNombreCompleto) {
        this.profesionalNombreCompleto = profesionalNombreCompleto;
    }

    public Long getEspecialidadId() {
        return especialidadId;
    }

    public void setEspecialidadId(Long especialidadId) {
        this.especialidadId = especialidadId;
    }

    public String getEspecialidadNombre() {
        return especialidadNombre;
    }

    public void setEspecialidadNombre(String especialidadNombre) {
        this.especialidadNombre = especialidadNombre;
    }

    public Long getSedeId() {
        return sedeId;
    }

    public void setSedeId(Long sedeId) {
        this.sedeId = sedeId;
    }

    public String getSedeNombre() {
        return sedeNombre;
    }

    public void setSedeNombre(String sedeNombre) {
        this.sedeNombre = sedeNombre;
    }

    public LocalDate getFechaDesde() {
        return fechaDesde;
    }

    public void setFechaDesde(LocalDate fechaDesde) {
        this.fechaDesde = fechaDesde;
    }

    public LocalDate getFechaHasta() {
        return fechaHasta;
    }

    public void setFechaHasta(LocalDate fechaHasta) {
        this.fechaHasta = fechaHasta;
    }

    public List<TurnoResponseDTO> getSlots() {
        return slots;
    }

    public void setSlots(List<TurnoResponseDTO> slots) {
        this.slots = slots;
    }
}

