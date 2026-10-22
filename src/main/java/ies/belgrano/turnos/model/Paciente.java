package ies.belgrano.turnos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "pacientes")
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Vinculación con el Usuario (importante para el login y registro)
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "usuario_id", referencedColumnName = "id")
    private Usuario usuario;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(nullable = false)
    private String nombre; // Corregido el "nombre;1"

    @NotBlank(message = "El apellido es obligatorio")
    @Column(nullable = false)
    private String apellido;

    // Cambiado CUIL por DNI, como lo pediste
    @NotBlank(message = "El DNI es obligatorio")
    @Column(nullable = false, unique = true)
    private String dni;

    @Column
    private String email;

    @Column
    private String telefono;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "genero")
    private String genero;

    @Column(name = "tiene_obra_social")
    private Boolean tieneObraSocial;

    @Column(name = "proveedor_obra_social")
    private String proveedorObraSocial;

    @Column(name = "numero_afiliado")
    private String numeroAfiliado;

    @Column(name = "plan_obra_social")
    private String planObraSocial;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "obra_social_id")
    private ObraSocial obraSocial;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id")
    private Paciente tutor;

    public Paciente() {
    }

    // Constructor actualizado con DNI en lugar de CUIL
    public Paciente(Long id, String nombre, String apellido, String dni, String email, String telefono, LocalDate fechaNacimiento, ObraSocial obraSocial) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.email = email;
        this.telefono = telefono;
        this.fechaNacimiento = fechaNacimiento;
        this.obraSocial = obraSocial;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
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

    // Getters y Setters para DNI
    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
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

    public ObraSocial getObraSocial() {
        return obraSocial;
    }

    public void setObraSocial(ObraSocial obraSocial) {
        this.obraSocial = obraSocial;
    }

    public Paciente getTutor() {
        return tutor;
    }

    public void setTutor(Paciente tutor) {
        this.tutor = tutor;
    }

    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }

    public Boolean getTieneObraSocial() { return tieneObraSocial; }
    public void setTieneObraSocial(Boolean tieneObraSocial) { this.tieneObraSocial = tieneObraSocial; }

    public String getProveedorObraSocial() { return proveedorObraSocial; }
    public void setProveedorObraSocial(String proveedorObraSocial) { this.proveedorObraSocial = proveedorObraSocial; }

    public String getNumeroAfiliado() { return numeroAfiliado; }
    public void setNumeroAfiliado(String numeroAfiliado) { this.numeroAfiliado = numeroAfiliado; }

    public String getPlanObraSocial() { return planObraSocial; }
    public void setPlanObraSocial(String planObraSocial) { this.planObraSocial = planObraSocial; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Paciente paciente = (Paciente) o;
        return Objects.equals(id, paciente.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}