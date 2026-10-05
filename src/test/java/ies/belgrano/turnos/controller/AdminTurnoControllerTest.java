package ies.belgrano.turnos.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
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
import ies.belgrano.turnos.dto.ReservaPresencialRequestDTO;
import ies.belgrano.turnos.model.Especialidad;
import ies.belgrano.turnos.model.Paciente;
import ies.belgrano.turnos.model.Profesional;
import ies.belgrano.turnos.model.Rol;
import ies.belgrano.turnos.model.Sede;
import ies.belgrano.turnos.model.Usuario;
import ies.belgrano.turnos.repository.EspecialidadRepository;
import ies.belgrano.turnos.repository.PacienteRepository;
import ies.belgrano.turnos.repository.ProfesionalRepository;
import ies.belgrano.turnos.repository.SedeRepository;
import ies.belgrano.turnos.repository.UsuarioRepository;
import ies.belgrano.turnos.security.JwtService;

@SpringBootTest(classes = TurnosApplication.class)
@TestPropertySource(properties = "spring.sql.init.mode=never")
@Transactional
class AdminTurnoControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

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

    private MockMvc mockMvc;
    private Paciente paciente;
    private Profesional profesional;
    private Especialidad especialidad;
    private Sede sede;
    private String tokenAdmin;
    private String tokenPaciente;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        paciente = pacienteRepository.save(new Paciente(
                null, "Mateo", "González", "55333444", "mateo@gmail.com",
                "2614001111", LocalDate.of(2018, 8, 10), null
        ));

        especialidad = especialidadRepository.save(new Especialidad("Pediatría", "Atención médica integral pediátrica"));
        profesional = profesionalRepository.save(new Profesional(
                null, "María", "Gómez", "25888777", "M-54321",
                "maria.gomez@notti.gob.ar", "2615556662"
        ));
        profesional.getEspecialidades().add(especialidad);

        sede = sedeRepository.save(new Sede("Hospital Notti", "Av. Bandera de los Andes 2603", "2614132000", "Guaymallén"));

        Usuario admin = new Usuario("admin@saludmza.gob.ar", "hashPassword", Rol.ADMINISTRADOR);
        usuarioRepository.save(admin);
        tokenAdmin = jwtService.generarToken(admin);

        Usuario pacienteUser = new Usuario("mateo@gmail.com", "hashPassword", Rol.PACIENTE);
        pacienteUser.setPaciente(paciente);
        usuarioRepository.save(pacienteUser);
        tokenPaciente = jwtService.generarToken(pacienteUser);
    }

    @Test
    @DisplayName("POST /api/v1/admin/turnos/reserva-presencial - Reserva exitosa por personal administrativo retorna 201 Created")
    void testReservaPresencial_Exitoso_Retorna201Created() throws Exception {
        LocalDateTime fechaFutura = LocalDateTime.now().plusDays(4).withHour(10).withMinute(0).withSecond(0).withNano(0);

        ReservaPresencialRequestDTO request = new ReservaPresencialRequestDTO(
                paciente.getId(),
                profesional.getId(),
                especialidad.getId(),
                sede.getId(),
                fechaFutura,
                "Control pediátrico semestral",
                "Agendado presencialmente en mostrador central"
        );

        mockMvc.perform(post("/api/v1/admin/turnos/reserva-presencial")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.estado", is("RESERVADO")))
                .andExpect(jsonPath("$.pacienteId", is(paciente.getId().intValue())))
                .andExpect(jsonPath("$.pacienteNombreCompleto", is("Mateo González")))
                .andExpect(jsonPath("$.profesionalId", is(profesional.getId().intValue())))
                .andExpect(jsonPath("$.profesionalNombreCompleto", is("María Gómez")))
                .andExpect(jsonPath("$.especialidadNombre", is("Pediatría")))
                .andExpect(jsonPath("$.sedeNombre", is("Hospital Notti")))
                .andExpect(jsonPath("$.motivoConsulta", is("Control pediátrico semestral")))
                .andExpect(jsonPath("$.observaciones", is("Agendado presencialmente en mostrador central")));
    }

    @Test
    @DisplayName("POST /api/v1/admin/turnos/reserva-presencial - Retorna 400 Bad Request ante campos faltantes o fecha pasada")
    void testReservaPresencial_DatosInvalidos_Retorna400BadRequest() throws Exception {
        LocalDateTime fechaPasada = LocalDateTime.now().minusDays(2);

        ReservaPresencialRequestDTO request = new ReservaPresencialRequestDTO(
                null, // pacienteId nulo
                profesional.getId(),
                especialidad.getId(),
                sede.getId(),
                fechaPasada, // fecha en el pasado
                "",
                ""
        );

        mockMvc.perform(post("/api/v1/admin/turnos/reserva-presencial")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Datos de Solicitud Inválidos")))
                .andExpect(jsonPath("$.detalles.pacienteId", notNullValue()))
                .andExpect(jsonPath("$.detalles.fechaHora", is("La fecha y hora del turno deben ser en el futuro")));
    }

    @Test
    @DisplayName("POST /api/v1/admin/turnos/reserva-presencial - Retorna 404 Not Found si el paciente o profesional no existen")
    void testReservaPresencial_PacienteInexistente_Retorna404NotFound() throws Exception {
        LocalDateTime fechaFutura = LocalDateTime.now().plusDays(2).withHour(9).withMinute(0).withSecond(0).withNano(0);
        Long pacienteIdInexistente = 888888L;

        ReservaPresencialRequestDTO request = new ReservaPresencialRequestDTO(
                pacienteIdInexistente,
                profesional.getId(),
                especialidad.getId(),
                sede.getId(),
                fechaFutura,
                "Consulta general",
                "Observación"
        );

        mockMvc.perform(post("/api/v1/admin/turnos/reserva-presencial")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Recurso No Encontrado")))
                .andExpect(jsonPath("$.message", containsString("No se encontró el paciente con ID: " + pacienteIdInexistente)));
    }

    @Test
    @DisplayName("POST /api/v1/admin/turnos/reserva-presencial - Retorna 409 Conflict ante solapamiento de turno del profesional")
    void testReservaPresencial_ConflictoHorario_Retorna409Conflict() throws Exception {
        LocalDateTime fechaFutura = LocalDateTime.now().plusDays(3).withHour(15).withMinute(0).withSecond(0).withNano(0);

        ReservaPresencialRequestDTO request1 = new ReservaPresencialRequestDTO(
                paciente.getId(),
                profesional.getId(),
                especialidad.getId(),
                sede.getId(),
                fechaFutura,
                "Primera consulta",
                ""
        );

        // Agendamiento inicial exitoso
        mockMvc.perform(post("/api/v1/admin/turnos/reserva-presencial")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        // Segundo intento de agendamiento en el MISMO horario y para el MISMO profesional
        ReservaPresencialRequestDTO request2 = new ReservaPresencialRequestDTO(
                paciente.getId(),
                profesional.getId(),
                especialidad.getId(),
                sede.getId(),
                fechaFutura,
                "Intento de turno superpuesto",
                ""
        );

        mockMvc.perform(post("/api/v1/admin/turnos/reserva-presencial")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Conflicto de Horario")))
                .andExpect(jsonPath("$.message", containsString("El profesional seleccionado ya posee un turno agendado")));
    }

    @Test
    @DisplayName("POST /api/v1/admin/turnos/reserva-presencial - Retorna 403 Forbidden cuando el usuario autenticado es PACIENTE")
    void testReservaPresencial_AccesoDenegadoParaPaciente_Retorna403Forbidden() throws Exception {
        LocalDateTime fechaFutura = LocalDateTime.now().plusDays(2).withHour(11).withMinute(0).withSecond(0).withNano(0);

        ReservaPresencialRequestDTO request = new ReservaPresencialRequestDTO(
                paciente.getId(),
                profesional.getId(),
                especialidad.getId(),
                sede.getId(),
                fechaFutura,
                "Consulta",
                ""
        );

        mockMvc.perform(post("/api/v1/admin/turnos/reserva-presencial")
                        .header("Authorization", "Bearer " + tokenPaciente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    @Test
    @DisplayName("POST /api/v1/admin/turnos/reserva-presencial - Retorna 401 Unauthorized sin token JWT")
    void testReservaPresencial_SinAutenticacion_Retorna401Unauthorized() throws Exception {
        LocalDateTime fechaFutura = LocalDateTime.now().plusDays(2).withHour(11).withMinute(0).withSecond(0).withNano(0);

        ReservaPresencialRequestDTO request = new ReservaPresencialRequestDTO(
                paciente.getId(),
                profesional.getId(),
                especialidad.getId(),
                sede.getId(),
                fechaFutura,
                "Consulta",
                ""
        );

        mockMvc.perform(post("/api/v1/admin/turnos/reserva-presencial")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));
    }
}
