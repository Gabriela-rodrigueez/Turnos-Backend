package ies.belgrano.turnos.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

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
import ies.belgrano.turnos.dto.LoginRequestDTO;
import ies.belgrano.turnos.dto.RegistroRequestDTO;
import ies.belgrano.turnos.model.ObraSocial;
import ies.belgrano.turnos.model.Paciente;
import ies.belgrano.turnos.model.Profesional;
import ies.belgrano.turnos.model.Rol;
import ies.belgrano.turnos.model.Usuario;
import ies.belgrano.turnos.repository.ObraSocialRepository;
import ies.belgrano.turnos.repository.PacienteRepository;
import ies.belgrano.turnos.repository.ProfesionalRepository;
import ies.belgrano.turnos.repository.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Map;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest(classes = TurnosApplication.class)
@TestPropertySource(properties = "spring.sql.init.mode=never")
@Transactional
class AuthControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObraSocialRepository obraSocialRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ProfesionalRepository profesionalRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("Caso Exitoso: Registro de usuario y posterior login con generación de JWT")
    void testRegistroYLoginExitoso() throws Exception {
        ObraSocial os = obraSocialRepository.save(new ObraSocial("OSEP Test", "OSEP-TEST"));

        RegistroRequestDTO registroReq = new RegistroRequestDTO(
                "Juan",
                "Pérez",
                "39999888",
                "nuevo.paciente@gmail.com",
                "Password123",
                "261555999",
                LocalDate.of(1995, 8, 25),
                os.getId()
        );

        // 1. Registro (POST 201)
        String regResponse = mockMvc.perform(post("/api/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.email", is("nuevo.paciente@gmail.com")))
                .andExpect(jsonPath("$.rol", is("PACIENTE")))
                .andExpect(jsonPath("$.pacienteId", notNullValue()))
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(regResponse).get("token").asText();

        // 2. Login con DNI del paciente (POST 200)
        LoginRequestDTO loginReq = LoginRequestDTO.paraPaciente("39999888", "Password123");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.email", is("nuevo.paciente@gmail.com")))
                .andExpect(jsonPath("$.rol", is("PACIENTE")));

        // 3. /me Endpoint (GET 200 con Token)
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("nuevo.paciente@gmail.com")))
                .andExpect(jsonPath("$.rol", is("PACIENTE")));
    }

    @Test
    @DisplayName("Caso Exitoso Polimórfico: Login de Paciente utilizando DNI en el payload")
    void testLoginPolimorficoConDniPaciente() throws Exception {
        ObraSocial os = obraSocialRepository.save(new ObraSocial("OSEP DNI", "OSEP-DNI"));

        RegistroRequestDTO registroReq = new RegistroRequestDTO(
                "Carlos",
                "Gómez",
                "37111222",
                "carlos.gomez@test.com",
                "Password123",
                "261444111",
                LocalDate.of(1992, 4, 10),
                os.getId()
        );

        mockMvc.perform(post("/api/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroReq)))
                .andExpect(status().isCreated());

        // Enviar payload con clave "dni"
        Map<String, String> payloadDni = Map.of(
                "dni", "37111222",
                "password", "Password123"
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payloadDni)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.email", is("carlos.gomez@test.com")))
                .andExpect(jsonPath("$.rol", is("PACIENTE")));
    }

    @Test
    @DisplayName("Caso Exitoso Polimórfico: Login de Médico utilizando Matrícula en el payload")
    void testLoginPolimorficoConMatriculaMedico() throws Exception {
        Profesional prof = new Profesional(null, "Roberto", "Pérez", "20111222", "M-77889", "roberto.perez@hospital.com", "261555111");
        prof = profesionalRepository.save(prof);

        Usuario userMedico = new Usuario("roberto.perez@hospital.com", passwordEncoder.encode("Password123"), Rol.MEDICO);
        userMedico.setProfesional(prof);
        usuarioRepository.save(userMedico);

        // Enviar payload con clave "matricula"
        Map<String, String> payloadMatricula = Map.of(
                "matricula", "M-77889",
                "password", "Password123"
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payloadMatricula)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.profesionalId", is(prof.getId().intValue())))
                .andExpect(jsonPath("$.rol", is("MEDICO")));
    }

    @Test
    @DisplayName("Caso Exitoso Polimórfico: Login de Administrador utilizando Legajo en el payload")
    void testLoginPolimorficoConLegajoAdmin() throws Exception {
        Usuario adminUser = new Usuario("admin.test@saludmza.gob.ar", passwordEncoder.encode("Password123"), Rol.ADMINISTRADOR, "LEG-9988");
        usuarioRepository.save(adminUser);

        // Enviar payload con clave "legajo"
        Map<String, String> payloadLegajo = Map.of(
                "legajo", "LEG-9988",
                "password", "Password123"
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payloadLegajo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.email", is("admin.test@saludmza.gob.ar")))
                .andExpect(jsonPath("$.rol", is("ADMINISTRADOR")));
    }

    @Test
    @DisplayName("Caso 401 Unauthorized: Login con credenciales incorrectas")
    void testLoginPolimorficoCredencialesIncorrectasRetorna401() throws Exception {
        Map<String, String> payloadErroneo = Map.of(
                "dni", "99999999",
                "password", "ClaveErronea"
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payloadErroneo)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("No Autorizado")));
    }

    @Test
    @DisplayName("Caso 401 Unauthorized: Login sin ningún identificador provisto")
    void testLoginSinIdentificadorRetorna401() throws Exception {
        Map<String, String> payloadSinId = Map.of(
                "password", "Password123"
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payloadSinId)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("No Autorizado")));
    }

    @Test
    @DisplayName("Caso 401 Unauthorized: Solicitud sin cabecera Authorization a endpoint privado /me")
    void testAccesoSinTokenRetorna401() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("No Autorizado")));
    }
}
