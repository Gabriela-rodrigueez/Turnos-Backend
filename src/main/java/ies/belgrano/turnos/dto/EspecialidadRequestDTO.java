package ies.belgrano.turnos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO de solicitud para el alta y edición de especialidades médicas por el Administrador")
public class EspecialidadRequestDTO {

    @NotBlank(message = "El nombre de la especialidad es obligatorio")
    @Size(max = 255, message = "El nombre no puede superar los 255 caracteres")
    @Schema(description = "Nombre oficial de la especialidad médica", example = "Neurología", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nombre;

    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    @Schema(description = "Descripción clínica o alcance asistencial de la especialidad", example = "Diagnóstico y tratamiento de patologías del sistema nervioso central y periférico.")
    private String descripcion;

    public EspecialidadRequestDTO() {
    }

    public EspecialidadRequestDTO(String nombre) {
        this.nombre = nombre;
    }

    public EspecialidadRequestDTO(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}

