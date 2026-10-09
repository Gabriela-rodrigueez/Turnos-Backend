package ies.belgrano.turnos.dto;

import java.time.LocalDate;

import ies.belgrano.turnos.model.Paciente;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO de respuesta con información detallada de un paciente")
public class PacienteResponseDTO {

    @Schema(description = "Identificador único del paciente", example = "1")
    private Long id;

    @Schema(description = "Nombre(s) del paciente", example = "Juan")
    private String nombre;

    @Schema(description = "Apellido(s) del paciente", example = "González")
    private String apellido;

    @Schema(description = "Número de CUIL del paciente", example = "27351112229")
    private String cuil;

    @Schema(description = "Correo electrónico del paciente", example = "juan.gonzalez@gmail.com")
    private String email;

    @Schema(description = "Teléfono de contacto del paciente", example = "2614001111")
    private String telefono;

    @Schema(description = "Fecha de nacimiento del paciente (YYYY-MM-DD)", example = "1990-05-15")
    private LocalDate fechaNacimiento;

    @Schema(description = "ID de la obra social asociada", example = "1")
    private Long obraSocialId;

    @Schema(description = "Nombre de la obra social asociada", example = "OSEP Mendoza")
    private String obraSocialNombre;

    @Schema(description = "ID del tutor legal (en caso de ser menor de edad)", example = "null")
    private Long tutorId;

    @Schema(description = "Nombre completo del tutor legal", example = "null")
    private String tutorNombreCompleto;

    public PacienteResponseDTO() {
    }

    public PacienteResponseDTO(Long id, String nombre, String apellido, String cuil, String email, String telefono,
                               LocalDate fechaNacimiento, Long obraSocialId, String obraSocialNombre,
                               Long tutorId, String tutorNombreCompleto) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.cuil = cuil;
        this.email = email;
        this.telefono = telefono;
        this.fechaNacimiento = fechaNacimiento;
        this.obraSocialId = obraSocialId;
        this.obraSocialNombre = obraSocialNombre;
        this.tutorId = tutorId;
        this.tutorNombreCompleto = tutorNombreCompleto;
    }

    public static PacienteResponseDTO fromEntity(Paciente paciente) {
        if (paciente == null) {
            return null;
        }

        PacienteResponseDTO dto = new PacienteResponseDTO();
        dto.setId(paciente.getId());
        dto.setNombre(paciente.getNombre());
        dto.setApellido(paciente.getApellido());
        dto.setCuil(paciente.getCuil());
        dto.setEmail(paciente.getEmail());
        dto.setTelefono(paciente.getTelefono());
        dto.setFechaNacimiento(paciente.getFechaNacimiento());

        if (paciente.getObraSocial() != null) {
            dto.setObraSocialId(paciente.getObraSocial().getId());
            dto.setObraSocialNombre(paciente.getObraSocial().getNombre());
        }

        if (paciente.getTutor() != null) {
            dto.setTutorId(paciente.getTutor().getId());
            dto.setTutorNombreCompleto(paciente.getTutor().getNombre() + " " + paciente.getTutor().getApellido());
        }

        return dto;
    }

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

    public String getCuil() {
        return cuil;
    }

    public void setCuil(String cuil) {
        this.cuil = cuil;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public Long getObraSocialId() {
        return obraSocialId;
    }

    public void setObraSocialId(Long obraSocialId) {
        this.obraSocialId = obraSocialId;
    }

    public String getObraSocialNombre() {
        return obraSocialNombre;
    }

    public void setObraSocialNombre(String obraSocialNombre) {
        this.obraSocialNombre = obraSocialNombre;
    }

    public Long getTutorId() {
        return tutorId;
    }

    public void setTutorId(Long tutorId) {
        this.tutorId = tutorId;
    }

    public String getTutorNombreCompleto() {
        return tutorNombreCompleto;
    }

    public void setTutorNombreCompleto(String tutorNombreCompleto) {
        this.tutorNombreCompleto = tutorNombreCompleto;
    }
}
