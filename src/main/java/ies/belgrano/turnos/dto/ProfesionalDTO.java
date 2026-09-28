package ies.belgrano.turnos.dto;

public class ProfesionalDTO {
    
    private Long id;
    private String nombre;
    private String apellido;
    private String especialidad;
    private String sede;

    // Constructor vacío 
    public ProfesionalDTO() {
    }

    // Constructor 
    public ProfesionalDTO(Long id, String nombre, String apellido, String especialidad, String sede) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.especialidad = especialidad;
        this.sede = sede;
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

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public String getSede() {
        return sede;
    }

    public void setSede(String sede) {
        this.sede = sede;
    }
}