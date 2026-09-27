package ies.belgrano.turnos.dto;

public class EspecialidadDTO {
    
    private Long id;
    private String nombre;

    // Constructor vacío 
    public EspecialidadDTO() {
    }

    // Constructor 
    public EspecialidadDTO(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
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
}