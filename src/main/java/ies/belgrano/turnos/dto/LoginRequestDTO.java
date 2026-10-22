package ies.belgrano.turnos.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class LoginRequestDTO {

    @Schema(description = "DNI del paciente (7 u 8 dígitos)", example = "35111222")
    private String dni;

    @Schema(description = "Matrícula profesional médica", example = "M-12345")
    private String matricula;

    @Schema(description = "Número de legajo administrativo", example = "8841")
    private String legajo;

    @NotBlank(message = "La contraseña es obligatoria")
    @Schema(description = "Contraseña de acceso", example = "Password123")
    private String password;

    public LoginRequestDTO() {
    }

    public LoginRequestDTO(String dni, String matricula, String legajo, String password) {
        this.dni = dni;
        this.matricula = matricula;
        this.legajo = legajo;
        this.password = password;
    }

    public static LoginRequestDTO paraPaciente(String dni, String password) {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setDni(dni);
        dto.setPassword(password);
        return dto;
    }

    public static LoginRequestDTO paraMedico(String matricula, String password) {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setMatricula(matricula);
        dto.setPassword(password);
        return dto;
    }

    public static LoginRequestDTO paraAdmin(String legajo, String password) {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setLegajo(legajo);
        dto.setPassword(password);
        return dto;
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

    /**
     * Retorna el identificador no nulo provisto (DNI, Matrícula o Legajo).
     */
    @JsonIgnore
    public String getIdentificador() {
        if (dni != null && !dni.isBlank()) return dni.trim();
        if (matricula != null && !matricula.isBlank()) return matricula.trim();
        if (legajo != null && !legajo.isBlank()) return legajo.trim();
        return null;
    }
}
