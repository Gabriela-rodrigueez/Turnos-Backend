package ies.belgrano.turnos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para registrar la atención médica y evolución clínica de un turno")
public class RegistrarAtencionRequestDTO {

    @Schema(description = "Motivo de la consulta manifestado por el paciente", example = "Dolor torácico opresivo de 2 horas de evolución")
    @NotBlank(message = "El motivo de consulta es obligatorio")
    @Size(max = 500, message = "El motivo de consulta no puede superar los 500 caracteres")
    private String motivoConsulta;

    @Schema(description = "Diagnóstico clínico y notas médicas", example = "Hipertensión arterial grado 1. Se descarta síndrome coronario agudo.")
    @NotBlank(message = "El diagnóstico clínico es obligatorio")
    @Size(max = 2000, message = "El diagnóstico clínico no puede superar los 2000 caracteres")
    private String diagnostico;

    @Schema(description = "Plan terapéutico, recomendaciones y cuidados", example = "Dieta hiposódica, actividad física regular y control de presión en 15 días.")
    @Size(max = 2000, message = "El tratamiento no puede superar los 2000 caracteres")
    private String tratamiento;

    @Schema(description = "Recetas de medicamentos prescritos con posología", example = "Enalapril 10mg - 1 comprimido cada 12 horas por 30 días.")
    @Size(max = 2000, message = "Las recetas prescritas no pueden superar los 2000 caracteres")
    private String recetasPrescritas;

    @Schema(description = "Órdenes de laboratorio o imágenes solicitadas", example = "Electrocardiograma de control y laboratorio de rutina (perfil lipídico y glucemia).")
    @Size(max = 2000, message = "Los estudios solicitados no pueden superar los 2000 caracteres")
    private String estudiosSolicitados;

    public RegistrarAtencionRequestDTO() {
    }

    public RegistrarAtencionRequestDTO(String motivoConsulta, String diagnostico, String tratamiento,
                                       String recetasPrescritas, String estudiosSolicitados) {
        this.motivoConsulta = motivoConsulta;
        this.diagnostico = diagnostico;
        this.tratamiento = tratamiento;
        this.recetasPrescritas = recetasPrescritas;
        this.estudiosSolicitados = estudiosSolicitados;
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
