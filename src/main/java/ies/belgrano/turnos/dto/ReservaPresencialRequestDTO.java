package ies.belgrano.turnos.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

@Schema(description = "DTO de solicitud para agendar un turno presencial por parte del personal administrativo")
public class ReservaPresencialRequestDTO {

    @Schema(description = "Identificador único del turno/slot a reservar (debe estar en estado DISPONIBLE)", example = "10")
    private Long turnoId;

    @NotNull(message = "El ID del paciente es obligatorio")
    @Schema(description = "Identificador único del paciente", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long pacienteId;

    @Schema(description = "Identificador único del profesional médico", example = "1")
    private Long profesionalId;

    @Schema(description = "Identificador único de la especialidad médica", example = "1")
    private Long especialidadId;

    @Schema(description = "Identificador único de la sede hospitalaria", example = "1")
    private Long sedeId;

    @Future(message = "La fecha y hora del turno deben ser en el futuro")
    @Schema(description = "Fecha y hora solicitadas para el turno (Formato ISO-8601)", example = "2026-10-15T09:30:00")
    private LocalDateTime fechaHora;

    @Schema(description = "Motivo de la consulta médica presencial", example = "Control cardiológico presencial agendado en ventanilla")
    private String motivoConsulta;

    @Schema(description = "Observaciones administrativas adicionales", example = "Paciente concurre presencialmente con orden médica")
    private String observaciones;

    public ReservaPresencialRequestDTO() {
    }

    public ReservaPresencialRequestDTO(Long turnoId, Long pacienteId, String motivoConsulta, String observaciones) {
        this.turnoId = turnoId;
        this.pacienteId = pacienteId;
        this.motivoConsulta = motivoConsulta;
        this.observaciones = observaciones;
    }

    public ReservaPresencialRequestDTO(Long pacienteId, Long profesionalId, Long especialidadId, Long sedeId,
                                      LocalDateTime fechaHora, String motivoConsulta, String observaciones) {
        this.pacienteId = pacienteId;
        this.profesionalId = profesionalId;
        this.especialidadId = especialidadId;
        this.sedeId = sedeId;
        this.fechaHora = fechaHora;
        this.motivoConsulta = motivoConsulta;
        this.observaciones = observaciones;
    }

    public Long getTurnoId() {
        return turnoId;
    }

    public void setTurnoId(Long turnoId) {
        this.turnoId = turnoId;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(Long pacienteId) {
        this.pacienteId = pacienteId;
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

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getMotivoConsulta() {
        return motivoConsulta;
    }

    public void setMotivoConsulta(String motivoConsulta) {
        this.motivoConsulta = motivoConsulta;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
