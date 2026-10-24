package ies.belgrano.turnos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Detalle de un registro de atención médica en el historial clínico")
public class HistoriaClinicaResponseDTO {

    @Schema(description = "Identificador único del registro clínico", example = "1")
    private Long id;

    @Schema(description = "Identificador del paciente", example = "1")
    private Long pacienteId;

    @Schema(description = "Nombre y apellido del paciente", example = "Juan González")
    private String pacienteNombreCompleto;

    @Schema(description = "DNI del paciente", example = "35111222")
    private String pacienteDni;

    @Schema(description = "Identificador del profesional médico", example = "1")
    private Long profesionalId;

    @Schema(description = "Nombre y apellido del profesional", example = "Dr. Roberto Pérez")
    private String profesionalNombreCompleto;

    @Schema(description = "Matrícula profesional", example = "M-12345")
    private String profesionalMatricula;

    @Schema(description = "Especialidad médica en la que se brindó la atención", example = "Cardiología")
    private String especialidadNombre;

    @Schema(description = "Sede hospitalaria de atención", example = "Hospital Central de Mendoza")
    private String sedeNombre;

    @Schema(description = "Identificador del turno asociado (si aplica)", example = "2")
    private Long turnoId;

    @Schema(description = "Fecha y hora en que se registró la atención médica", example = "2026-10-23T10:30:00")
    private LocalDateTime fechaAtencion;

    @Schema(description = "Motivo de la consulta", example = "Control de hipertensión")
    private String motivoConsulta;

    @Schema(description = "Diagnóstico clínico", example = "Hipertensión arterial controlada")
    private String diagnostico;

    @Schema(description = "Plan terapéutico e indicaciones", example = "Continuar con medicación habitual")
    private String tratamiento;

    @Schema(description = "Recetas de medicamentos prescritos", example = "Enalapril 10mg")
    private String recetasPrescritas;

    @Schema(description = "Estudios solicitados", example = "ECG y laboratorio")
    private String estudiosSolicitados;

    public HistoriaClinicaResponseDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getPacienteDni() {
        return pacienteDni;
    }

    public void setPacienteDni(String pacienteDni) {
        this.pacienteDni = pacienteDni;
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

    public String getProfesionalMatricula() {
        return profesionalMatricula;
    }

    public void setProfesionalMatricula(String profesionalMatricula) {
        this.profesionalMatricula = profesionalMatricula;
    }

    public String getEspecialidadNombre() {
        return especialidadNombre;
    }

    public void setEspecialidadNombre(String especialidadNombre) {
        this.especialidadNombre = especialidadNombre;
    }

    public String getSedeNombre() {
        return sedeNombre;
    }

    public void setSedeNombre(String sedeNombre) {
        this.sedeNombre = sedeNombre;
    }

    public Long getTurnoId() {
        return turnoId;
    }

    public void setTurnoId(Long turnoId) {
        this.turnoId = turnoId;
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
