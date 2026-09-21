package ies.belgrano.turnos.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class ReservaTurnoRequestDTO {

    @NotNull(message = "El ID del paciente es obligatorio")
    private Long pacienteId;

    @NotNull(message = "El ID del profesional es obligatorio")
    private Long profesionalId;

    @NotNull(message = "El ID de la especialidad es obligatorio")
    private Long especialidadId;

    @NotNull(message = "El ID de la sede es obligatorio")
    private Long sedeId;

    @NotNull(message = "La fecha y hora del turno son obligatorias")
    @Future(message = "La fecha y hora del turno deben ser en el futuro")
    private LocalDateTime fechaHora;

    private String motivoConsulta;

    public ReservaTurnoRequestDTO() {
    }

    public ReservaTurnoRequestDTO(Long pacienteId, Long profesionalId, Long especialidadId, Long sedeId, LocalDateTime fechaHora, String motivoConsulta) {
        this.pacienteId = pacienteId;
        this.profesionalId = profesionalId;
        this.especialidadId = especialidadId;
        this.sedeId = sedeId;
        this.fechaHora = fechaHora;
        this.motivoConsulta = motivoConsulta;
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
}
