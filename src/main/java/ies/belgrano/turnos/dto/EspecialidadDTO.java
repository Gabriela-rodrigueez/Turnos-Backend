package ies.belgrano.turnos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO representativo de una especialidad médica hospitalaria")
public class EspecialidadDTO {

    @Schema(description = "Identificador único de la especialidad", example = "1")
    private Long id;

    @Schema(description = "Nombre oficial de la especialidad", example = "Cardiología")
    private String nombre;

    @Schema(description = "Descripción clínica o alcance de la especialidad", example = "Diagnóstico y tratamiento de afecciones del sistema cardiovascular.")
    private String descripcion;

    // Constructor vacío
    public EspecialidadDTO() {
    }

    // Constructor con id y nombre (retrocompatibilidad)
    public EspecialidadDTO(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    // Constructor completo
    public EspecialidadDTO(Long id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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