package ies.belgrano.turnos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Schema(description = "DTO de solicitud para generación masiva de slots de agenda médica hospitalaria")
public class GenerarSlotsRequestDTO {

    @NotNull(message = "El ID del profesional es obligatorio")
    @Schema(description = "Identificador único del profesional médico", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long profesionalId;

    @NotNull(message = "El ID de la especialidad es obligatorio")
    @Schema(description = "Identificador único de la especialidad médica", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long especialidadId;

    @NotNull(message = "El ID de la sede es obligatorio")
    @Schema(description = "Identificador único de la sede hospitalaria", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long sedeId;

    @NotNull(message = "La fecha desde es obligatoria")
    @FutureOrPresent(message = "La fecha desde debe ser hoy o en el futuro")
    @Schema(description = "Fecha inicial del rango de agenda (inclusivo)", example = "2026-10-20", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate fechaDesde;

    @NotNull(message = "La fecha hasta es obligatoria")
    @FutureOrPresent(message = "La fecha hasta debe ser hoy o en el futuro")
    @Schema(description = "Fecha final del rango de agenda (inclusivo)", example = "2026-10-25", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate fechaHasta;

    @NotNull(message = "La hora de inicio es obligatoria")
    @Schema(description = "Hora de inicio de atención para cada jornada", example = "08:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalTime horaInicio;

    @NotNull(message = "La hora de fin es obligatoria")
    @Schema(description = "Hora de fin de atención para cada jornada", example = "12:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalTime horaFin;

    @NotNull(message = "La duración en minutos es obligatoria")
    @Min(value = 5, message = "La duración mínima de cada slot debe ser de al menos 5 minutos")
    @Schema(description = "Duración en minutos de cada slot de consulta", example = "30", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer duracionMinutos;

    @Schema(description = "Días de la semana en los que se habilitará la agenda (por defecto lunes a viernes)", example = "[\"MONDAY\", \"WEDNESDAY\", \"FRIDAY\"]")
    private List<DayOfWeek> diasSemana;

    public GenerarSlotsRequestDTO() {
    }

    public GenerarSlotsRequestDTO(Long profesionalId, Long especialidadId, Long sedeId,
                                  LocalDate fechaDesde, LocalDate fechaHasta,
                                  LocalTime horaInicio, LocalTime horaFin,
                                  Integer duracionMinutos, List<DayOfWeek> diasSemana) {
        this.profesionalId = profesionalId;
        this.especialidadId = especialidadId;
        this.sedeId = sedeId;
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.duracionMinutos = duracionMinutos;
        this.diasSemana = diasSemana;
    }

    public Long getProfesionalId() {
        return profesionalId;
    }

    public void setProfesionalId(Long profesionalId) {
        this.profesionalId = profesionalId;
    }

    public Long getEspecialidadId() {
        return especialidadId;
    }

    public void setEspecialidadId(Long especialidadId) {
        this.especialidadId = especialidadId;
    }

    public Long getSedeId() {
        return sedeId;
    }

    public void setSedeId(Long sedeId) {
        this.sedeId = sedeId;
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

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public Integer getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(Integer duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    public List<DayOfWeek> getDiasSemana() {
        return diasSemana;
    }

    public void setDiasSemana(List<DayOfWeek> diasSemana) {
        this.diasSemana = diasSemana;
    }
}

