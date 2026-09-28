package ies.belgrano.turnos.dto;

public class SedeDTO {
    
    private Long id;
    private String nombre;
    private String direccion;

    // Constructor vacío
    public SedeDTO() {
    }

    // Constructor
    public SedeDTO(Long id, String nombre, String direccion) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
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

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
}