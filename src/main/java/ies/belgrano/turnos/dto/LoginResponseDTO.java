package ies.belgrano.turnos.dto;

import ies.belgrano.turnos.model.Rol;

public class LoginResponseDTO {

    private String token;
    private String tipoToken = "Bearer";
    private Long usuarioId;
    private Long pacienteId;
    private Long profesionalId;
    private String nombreCompleto;
    private String email;
    private Rol rol;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(String token, Long usuarioId, Long pacienteId, Long profesionalId, String nombreCompleto, String email, Rol rol) {
        this.token = token;
        this.tipoToken = "Bearer";
        this.usuarioId = usuarioId;
        this.pacienteId = pacienteId;
        this.profesionalId = profesionalId;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.rol = rol;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTipoToken() {
        return tipoToken;
    }

    public void setTipoToken(String tipoToken) {
        this.tipoToken = tipoToken;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(Long pacienteId) {
        this.pacienteId = pacienteId;
    }

    public Long getProfesionalId() {
        return profesionalId;
    }

    public void setProfesionalId(Long profesionalId) {
        this.profesionalId = profesionalId;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }
}
