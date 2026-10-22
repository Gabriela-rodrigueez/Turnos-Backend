package ies.belgrano.turnos.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RegistroRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    private String apellido;

    @JsonProperty("dni")
    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "^\\d{8}$", message = "El DNI debe tener 8 dígitos numéricos")
    private String dni;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El formato de correo electrónico es inválido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 30, message = "La contraseña debe tener entre 8 y 30 caracteres")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z]).{8,}$", message = "La contraseña debe contener al menos una letra y un número")
    private String password;

    private String telefono;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
    @JsonProperty("fechaNacimiento")
    private LocalDate fechaNacimiento;

    private Long obraSocialId;

    @JsonProperty("gender")
    private String gender;

    @JsonProperty("has_insurance")
    private String hasInsurance;

    private String provider;

    @JsonProperty("affiliate_num")
    private String affiliateNum;

    @JsonProperty("plan")
    private String plan;

    public RegistroRequestDTO() {
    }

    
    public RegistroRequestDTO(String nombre, String apellido, String dni, String email, String password, String telefono, LocalDate fechaNacimiento, Long obraSocialId) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.email = email;
        this.password = password;
        this.telefono = telefono;
        this.fechaNacimiento = fechaNacimiento;
        this.obraSocialId = obraSocialId;
    }

    // --- GETTERS Y SETTERS ---
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public Long getObraSocialId() { return obraSocialId; }
    public void setObraSocialId(Long obraSocialId) { this.obraSocialId = obraSocialId; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getHasInsurance() { return hasInsurance; }
    public void setHasInsurance(String  hasInsurance) { this.hasInsurance = hasInsurance; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getAffiliateNum() { return affiliateNum; }
    public void setAffiliateNum(String affiliateNum) { this.affiliateNum = affiliateNum; }

    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }
}