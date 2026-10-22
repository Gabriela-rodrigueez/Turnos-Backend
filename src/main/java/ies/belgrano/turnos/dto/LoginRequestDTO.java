package ies.belgrano.turnos.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequestDTO {

    private String dni;   
    private String matricula;
    private String legajo;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    public LoginRequestDTO() {
    }

    public LoginRequestDTO(String dni, String matricula,String legajo, String password) {
        this.dni = dni;
        this.matricula = matricula;
        this.legajo = legajo;
        this.password = password;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getLegajo() {
        return legajo;
    }

    public void setLegajo(String legajo) {
        this.legajo = legajo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}