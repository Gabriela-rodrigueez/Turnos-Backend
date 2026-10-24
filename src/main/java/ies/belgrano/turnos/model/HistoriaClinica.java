package ies.belgrano.turnos.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "historia_clinica")
public class HistoriaClinica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El paciente es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @NotNull(message = "El profesional médico es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "profesional_id", nullable = false)
    private Profesional profesional;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "turno_id")
    private Turno turno;

    @NotNull(message = "La fecha de atención es obligatoria")
    @Column(name = "fecha_atencion", nullable = false)
    private LocalDateTime fechaAtencion;

    @NotBlank(message = "El motivo de consulta es obligatorio")
    @Column(name = "motivo_consulta", nullable = false, length = 500)
    private String motivoConsulta;

    @NotBlank(message = "El diagnóstico clínico es obligatorio")
    @Column(name = "diagnostico", nullable = false, length = 2000)
    private String diagnostico;

    @Column(name = "tratamiento", length = 2000)
    private String tratamiento;

    @Column(name = "recetas_prescritas", length = 2000)
    private String recetasPrescritas;

    @Column(name = "estudios_solicitados", length = 2000)
    private String estudiosSolicitados;

    public HistoriaClinica() {
        this.fechaAtencion = LocalDateTime.now();
    }

    public HistoriaClinica(Paciente paciente, Profesional profesional, Turno turno,
                           String motivoConsulta, String diagnostico, String tratamiento,
                           String recetasPrescritas, String estudiosSolicitados) {
        this.paciente = paciente;
        this.profesional = profesional;
        this.turno = turno;
        this.fechaAtencion = LocalDateTime.now();
        this.motivoConsulta = motivoConsulta;
        this.diagnostico = diagnostico;
        this.tratamiento = tratamiento;
        this.recetasPrescritas = recetasPrescritas;
        this.estudiosSolicitados = estudiosSolicitados;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Turno getTurno() {
        return turno;
    }

    public void setTurno(Turno turno) {
        this.turno = turno;
    }

    public LocalDateTime getFechaAtencion() {
        return fechaAtencion;
    }

    public void setFechaAtencion(LocalDateTime fechaAtencion) {
        this.fechaAtencion = fechaAtencion;
    }

    public String getMotivoConsulta() {
        return motivoConsulta;
    }

    public void setMotivoConsulta(String motivoConsulta) {
        this.motivoConsulta = motivoConsulta;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }

    public String getTratamiento() {
        return tratamiento;
    }

    public void setTratamiento(String tratamiento) {
        this.tratamiento = tratamiento;
    }

    public String getRecetasPrescritas() {
        return recetasPrescritas;
    }

    public void setRecetasPrescritas(String recetasPrescritas) {
        this.recetasPrescritas = recetasPrescritas;
    }

    public String getEstudiosSolicitados() {
        return estudiosSolicitados;
    }

    public void setEstudiosSolicitados(String estudiosSolicitados) {
        this.estudiosSolicitados = estudiosSolicitados;
    }
}
