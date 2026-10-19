package ies.belgrano.turnos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "turnos")
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La fecha y hora de inicio del turno son obligatorias")
    @Column(name = "fecha_hora_inicio", nullable = false)
    private LocalDateTime fechaHoraInicio;

    @NotNull(message = "La fecha y hora de fin del turno son obligatorias")
    @Column(name = "fecha_hora_fin", nullable = false)
    private LocalDateTime fechaHoraFin;

    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoTurno estado = EstadoTurno.DISPONIBLE;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @NotNull(message = "El profesional es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "profesional_id", nullable = false)
    private Profesional profesional;

    @NotNull(message = "La especialidad es obligatoria")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "especialidad_id", nullable = false)
    private Especialidad especialidad;

    @NotNull(message = "La sede es obligatoria")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sede_id", nullable = false)
    private Sede sede;

    @Column(name = "motivo_consulta")
    private String motivoConsulta;

    @Column(length = 1000)
    private String observaciones;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.fechaHoraInicio == null && this.fechaHora != null) {
            this.fechaHoraInicio = this.fechaHora;
        }
        if (this.fechaHoraFin == null && this.fechaHoraInicio != null) {
            this.fechaHoraFin = this.fechaHoraInicio.plusMinutes(15);
        }
        if (this.fechaHora == null && this.fechaHoraInicio != null) {
            this.fechaHora = this.fechaHoraInicio;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (this.fechaHoraInicio == null && this.fechaHora != null) {
            this.fechaHoraInicio = this.fechaHora;
        }
        if (this.fechaHoraFin == null && this.fechaHoraInicio != null) {
            this.fechaHoraFin = this.fechaHoraInicio.plusMinutes(15);
        }
        if (this.fechaHora == null && this.fechaHoraInicio != null) {
            this.fechaHora = this.fechaHoraInicio;
        }
    }

    public Turno() {
    }

    public Turno(Long id, LocalDateTime fechaHora, EstadoTurno estado, Paciente paciente, Profesional profesional, Especialidad especialidad, Sede sede, String motivoConsulta) {
        this.id = id;
        this.fechaHora = fechaHora;
        this.fechaHoraInicio = fechaHora;
        this.fechaHoraFin = fechaHora != null ? fechaHora.plusMinutes(15) : null;
        this.estado = estado != null ? estado : EstadoTurno.DISPONIBLE;
        this.paciente = paciente;
        this.profesional = profesional;
        this.especialidad = especialidad;
        this.sede = sede;
        this.motivoConsulta = motivoConsulta;
    }

    public Turno(Long id, LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin, EstadoTurno estado, Paciente paciente, Profesional profesional, Especialidad especialidad, Sede sede, String motivoConsulta) {
        this.id = id;
        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHoraFin = fechaHoraFin;
        this.fechaHora = fechaHoraInicio;
        this.estado = estado != null ? estado : EstadoTurno.DISPONIBLE;
        this.paciente = paciente;
        this.profesional = profesional;
        this.especialidad = especialidad;
        this.sede = sede;
        this.motivoConsulta = motivoConsulta;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFechaHoraInicio() {
        return fechaHoraInicio != null ? fechaHoraInicio : fechaHora;
    }

    public void setFechaHoraInicio(LocalDateTime fechaHoraInicio) {
        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHora = fechaHoraInicio;
        if (this.fechaHoraFin == null && fechaHoraInicio != null) {
            this.fechaHoraFin = fechaHoraInicio.plusMinutes(15);
        }
    }

    public LocalDateTime getFechaHoraFin() {
        return fechaHoraFin;
    }

    public void setFechaHoraFin(LocalDateTime fechaHoraFin) {
        this.fechaHoraFin = fechaHoraFin;
    }

    public LocalDateTime getFechaHora() {
        return fechaHoraInicio != null ? fechaHoraInicio : fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
        if (this.fechaHoraInicio == null) {
            this.fechaHoraInicio = fechaHora;
        }
        if (this.fechaHoraFin == null && fechaHora != null) {
            this.fechaHoraFin = fechaHora.plusMinutes(15);
        }
    }

    public EstadoTurno getEstado() {
        return estado;
    }

    public void setEstado(EstadoTurno estado) {
        this.estado = estado;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public Profesional getProfesional() {
        return profesional;
    }

    public void setProfesional(Profesional profesional) {
        this.profesional = profesional;
    }

    public Especialidad getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(Especialidad especialidad) {
        this.especialidad = especialidad;
    }

    public Sede getSede() {
        return sede;
    }

    public void setSede(Sede sede) {
        this.sede = sede;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Turno turno = (Turno) o;
        return Objects.equals(id, turno.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
