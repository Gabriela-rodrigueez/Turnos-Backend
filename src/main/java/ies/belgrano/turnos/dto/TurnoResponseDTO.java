package ies.belgrano.turnos.dto;

import ies.belgrano.turnos.model.EstadoTurno;
import ies.belgrano.turnos.model.Turno;

import java.time.LocalDateTime;

public class TurnoResponseDTO {

    private Long id;
    private LocalDateTime fechaHora;
    private EstadoTurno estado;
    private Long pacienteId;
    private String pacienteNombreCompleto;
    private Long profesionalId;
    private String profesionalNombreCompleto;
    private Long especialidadId;
    private String especialidadNombre;
    private Long sedeId;
    private String sedeNombre;
    private String motivoConsulta;
    private String observaciones;
    private LocalDateTime fechaCreacion;

    public TurnoResponseDTO() {
    }

    public static TurnoResponseDTO fromEntity(Turno turno) {
        TurnoResponseDTO dto = new TurnoResponseDTO();
        dto.setId(turno.getId());
        dto.setFechaHora(turno.getFechaHora());
        dto.setEstado(turno.getEstado());
        
        if (turno.getPaciente() != null) {
            dto.setPacienteId(turno.getPaciente().getId());
            dto.setPacienteNombreCompleto(turno.getPaciente().getNombre() + " " + turno.getPaciente().getApellido());
        }

        if (turno.getProfesional() != null) {
            dto.setProfesionalId(turno.getProfesional().getId());
            dto.setProfesionalNombreCompleto(turno.getProfesional().getNombre() + " " + turno.getProfesional().getApellido());
        }

        if (turno.getEspecialidad() != null) {
            dto.setEspecialidadId(turno.getEspecialidad().getId());
            dto.setEspecialidadNombre(turno.getEspecialidad().getNombre());
        }

        if (turno.getSede() != null) {
            dto.setSedeId(turno.getSede().getId());
            dto.setSedeNombre(turno.getSede().getNombre());
        }

        dto.setMotivoConsulta(turno.getMotivoConsulta());
        dto.setObservaciones(turno.getObservaciones());
        dto.setFechaCreacion(turno.getFechaCreacion());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public EstadoTurno getEstado() {
        return estado;
    }

    public void setEstado(EstadoTurno estado) {
        this.estado = estado;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(Long pacienteId) {
        this.pacienteId = pacienteId;
    }

    public String getPacienteNombreCompleto() {
        return pacienteNombreCompleto;
    }

    public void setPacienteNombreCompleto(String pacienteNombreCompleto) {
        this.pacienteNombreCompleto = pacienteNombreCompleto;
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

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
