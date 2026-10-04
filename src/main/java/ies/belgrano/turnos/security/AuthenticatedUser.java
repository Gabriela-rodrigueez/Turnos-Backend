package ies.belgrano.turnos.security;

import ies.belgrano.turnos.model.Rol;

public class AuthenticatedUser {

    private final Long usuarioId;
    private final String email;
    private final Rol rol;
    private final Long pacienteId;
    private final Long profesionalId;

    public AuthenticatedUser(Long usuarioId, String email, Rol rol, Long pacienteId, Long profesionalId) {
        this.usuarioId = usuarioId;
        this.email = email;
        this.rol = rol;
        this.pacienteId = pacienteId;
        this.profesionalId = profesionalId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public String getEmail() {
        return email;
    }

    public Rol getRol() {
        return rol;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public Long getProfesionalId() {
        return profesionalId;
    }

    public boolean esPaciente() {
        return Rol.PACIENTE == rol;
    }

    public boolean esMedico() {
        return Rol.MEDICO == rol;
    }

    public boolean esAdministrador() {
        return Rol.ADMINISTRADOR == rol;
    }
}
