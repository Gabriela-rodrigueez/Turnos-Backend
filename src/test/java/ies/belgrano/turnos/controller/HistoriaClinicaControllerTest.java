package ies.belgrano.turnos.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import ies.belgrano.turnos.TurnosApplication;
import ies.belgrano.turnos.dto.RegistrarAtencionRequestDTO;
import ies.belgrano.turnos.model.Especialidad;
import ies.belgrano.turnos.model.EstadoTurno;
import ies.belgrano.turnos.model.Paciente;
import ies.belgrano.turnos.model.Profesional;
import ies.belgrano.turnos.model.Rol;
import ies.belgrano.turnos.model.Sede;
import ies.belgrano.turnos.model.Turno;
import ies.belgrano.turnos.model.Usuario;
import ies.belgrano.turnos.repository.EspecialidadRepository;
import ies.belgrano.turnos.repository.HistoriaClinicaRepository;
import ies.belgrano.turnos.repository.PacienteRepository;
import ies.belgrano.turnos.repository.ProfesionalRepository;
import ies.belgrano.turnos.repository.SedeRepository;
import ies.belgrano.turnos.repository.TurnoRepository;
import ies.belgrano.turnos.repository.UsuarioRepository;
import ies.belgrano.turnos.security.JwtService;

@SpringBootTest(classes = TurnosApplication.class)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:turnosdb_hc_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class HistoriaClinicaControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TurnoRepository turnoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ProfesionalRepository profesionalRepository;

    @Autowired
    private EspecialidadRepository especialidadRepository;

    @Autowired
    private SedeRepository sedeRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private HistoriaClinicaRepository historiaClinicaRepository;

    @Autowired
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    private MockMvc mockMvc;

    private Paciente paciente1;
    private Paciente paciente2;
    private Profesional profesional1;
    private Profesional profesional2;
    private Turno turno1;

    private String tokenMedico1;
    private String tokenMedico2;
    private String tokenPaciente1;
    private String tokenPaciente2;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        Especialidad esp = especialidadRepository.findById(1L)
                .orElseGet(() -> especialidadRepository.save(new Especialidad("Infectología Experimental", "Atención")));
        Sede sede = sedeRepository.findById(1L)
                .orElseGet(() -> sedeRepository.save(new Sede("Sede Experimental", "San Martín 100", "2614000000", "Capital")));

        profesional1 = profesionalRepository.save(new Profesional(null, "Roberto", "Pérez", "45111001", "M-HC101", "roberto_hc@test.com", "261"));
        profesional2 = profesionalRepository.save(new Profesional(null, "María", "Gómez", "45111002", "M-HC102", "maria_hc@test.com", "261"));

        paciente1 = pacienteRepository.save(new Paciente(null, "Juan", "González", "45111003", "juan_hc@test.com", "261", null, null));
        paciente2 = pacienteRepository.save(new Paciente(null, "Lucía", "Martínez", "45111004", "lucia_hc@test.com", "261", null, null));

        turno1 = new Turno();
        turno1.setProfesional(profesional1);
        turno1.setPaciente(paciente1);
        turno1.setEspecialidad(esp);
        turno1.setSede(sede);
        turno1.setEstado(EstadoTurno.RESERVADO);
        turno1.setFechaHoraInicio(LocalDateTime.now());
        turno1.setFechaHoraFin(LocalDateTime.now().plusMinutes(30));
        turno1 = turnoRepository.save(turno1);

        // Usuarios y tokens
        Usuario uMedico1 = new Usuario("medico1_hc@hospital.com", "pass", Rol.MEDICO);
        uMedico1.setProfesional(profesional1);
        usuarioRepository.save(uMedico1);
        tokenMedico1 = jwtService.generarToken(uMedico1);

        Usuario uMedico2 = new Usuario("medico2_hc@hospital.com", "pass", Rol.MEDICO);
        uMedico2.setProfesional(profesional2);
        usuarioRepository.save(uMedico2);
        tokenMedico2 = jwtService.generarToken(uMedico2);

        Usuario uPaciente1 = new Usuario("paciente1_hc@hospital.com", "pass", Rol.PACIENTE);
        uPaciente1.setPaciente(paciente1);
        usuarioRepository.save(uPaciente1);
        tokenPaciente1 = jwtService.generarToken(uPaciente1);

        Usuario uPaciente2 = new Usuario("paciente2_hc@hospital.com", "pass", Rol.PACIENTE);
        uPaciente2.setPaciente(paciente2);
        usuarioRepository.save(uPaciente2);
        tokenPaciente2 = jwtService.generarToken(uPaciente2);
    }

    @Test
    @DisplayName("POST /api/v1/medico/consultas/{turnoId}/atender - Registra atención exitosa con token del médico asignado")
    void atenderTurno_Exitoso() throws Exception {
        RegistrarAtencionRequestDTO request = new RegistrarAtencionRequestDTO(
                "Fiebre persistente",
                "Faringitis aguda",
                "Reposo e hidratación",
                "Amoxicilina 500mg cada 8hs",
                "Hemograma completo"
        );

        mockMvc.perform(post("/api/v1/medico/consultas/" + turno1.getId() + "/atender")
                        .header("Authorization", "Bearer " + tokenMedico1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.diagnostico", is("Faringitis aguda")))
                .andExpect(jsonPath("$.pacienteNombreCompleto", is("Juan González")))
                .andExpect(jsonPath("$.recetasPrescritas", is("Amoxicilina 500mg cada 8hs")));
    }

    @Test
    @DisplayName("POST /api/v1/medico/consultas/{turnoId}/atender - Devuelve 403 si un médico intenta atender un turno de otro profesional")
    void atenderTurno_OtroMedico_Forbidden() throws Exception {
        RegistrarAtencionRequestDTO request = new RegistrarAtencionRequestDTO(
                "Consulta", "Diagnóstico", null, null, null
        );

        mockMvc.perform(post("/api/v1/medico/consultas/" + turno1.getId() + "/atender")
                        .header("Authorization", "Bearer " + tokenMedico2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/medico/consultas/{turnoId}/atender - Devuelve 403 si un paciente intenta invocar el endpoint de atención médica")
    void atenderTurno_RolPaciente_Forbidden() throws Exception {
        RegistrarAtencionRequestDTO request = new RegistrarAtencionRequestDTO(
                "Consulta", "Diagnóstico", null, null, null
        );

        mockMvc.perform(post("/api/v1/medico/consultas/" + turno1.getId() + "/atender")
                        .header("Authorization", "Bearer " + tokenPaciente1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/historial-clinico/mi-historial - Retorna atenciones propias del paciente")
    void obtenerMiHistorial_Exitoso() throws Exception {
        // Primero registrar una atención
        RegistrarAtencionRequestDTO request = new RegistrarAtencionRequestDTO(
                "Control", "Hipertensión controlada", "Continuar tratamiento", null, null
        );
        mockMvc.perform(post("/api/v1/medico/consultas/" + turno1.getId() + "/atender")
                        .header("Authorization", "Bearer " + tokenMedico1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Consultar con token del paciente1
        mockMvc.perform(get("/api/v1/historial-clinico/mi-historial")
                        .header("Authorization", "Bearer " + tokenPaciente1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].diagnostico", is("Hipertensión controlada")));
    }

    @Test
    @DisplayName("GET /api/v1/historial-clinico/paciente/{id} - Devuelve 403 si paciente intenta consultar a otro paciente")
    void obtenerHistorialPaciente_OwnershipViolation_Forbidden() throws Exception {
        // Paciente 2 intenta consultar el historial del Paciente 1
        mockMvc.perform(get("/api/v1/historial-clinico/paciente/" + paciente1.getId())
                        .header("Authorization", "Bearer " + tokenPaciente2))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    @Test
    @DisplayName("GET /api/v1/historial-clinico/paciente/{id} - Permite a un médico consultar el historial de cualquier paciente")
    void obtenerHistorialPaciente_MedicoConsulta_Exitoso() throws Exception {
        mockMvc.perform(get("/api/v1/historial-clinico/paciente/" + paciente1.getId())
                        .header("Authorization", "Bearer " + tokenMedico1))
                .andExpect(status().isOk());
    }
}
