package ies.belgrano.turnos.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import ies.belgrano.turnos.dto.ReservaTurnoRequestDTO;
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
import ies.belgrano.turnos.security.JwtService;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest(classes = TurnosApplication.class)
@TestPropertySource(properties = "spring.sql.init.mode=never")
@Transactional
class TurnoControllerTest {

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

    private MockMvc mockMvc;
    private Paciente paciente;
    private Profesional profesional;
    private Especialidad especialidad;
    private Sede sede;
    private String tokenPaciente;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        paciente = pacienteRepository.save(new Paciente(null, "Carlos", "Gómez", "30111222", "carlos@gmail.com", "261444333", LocalDate.of(1985, 3, 20), null));
        especialidad = especialidadRepository.save(new Especialidad("Cardiología", "Estudio del corazón"));
        profesional = profesionalRepository.save(new Profesional(null, "Roberto", "Pérez", "20999888", "M-12345", "roberto@hospital.com", "261555666"));
        profesional.getEspecialidades().add(especialidad);
        sede = sedeRepository.save(new Sede("Hospital Notti", "Bandera de los Andes 2603", "2614132000", "Guaymallén"));

        Usuario usuario = new Usuario("carlos@gmail.com", "hash", Rol.PACIENTE);
        usuario.setId(200L);
        usuario.setPaciente(paciente);
        tokenPaciente = jwtService.generarToken(usuario);
    }

    @Test
    @DisplayName("Caso Exitoso: Reservar turno, consultar disponibilidad y cancelar turno")
    void testFlujoExitosoReservaDisponibilidadYCancelacion() throws Exception {
        LocalDateTime fechaFutura = LocalDateTime.now().plusDays(5).withHour(10).withMinute(0).withSecond(0).withNano(0);

        ReservaTurnoRequestDTO request = new ReservaTurnoRequestDTO(
                paciente.getId(),
                profesional.getId(),
                especialidad.getId(),
                sede.getId(),
                fechaFutura,
                "Control de presión arterial"
        );

        // 1. Reservar Turno (POST 201)
        String responseContent = mockMvc.perform(post("/api/v1/turnos/reserva")
                        .header("Authorization", "Bearer " + tokenPaciente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.estado", is("RESERVADO")))
                .andExpect(jsonPath("$.pacienteNombreCompleto", is("Carlos Gómez")))
                .andExpect(jsonPath("$.profesionalNombreCompleto", is("Roberto Pérez")))
                .andExpect(jsonPath("$.especialidadNombre", is("Cardiología")))
                .andExpect(jsonPath("$.sedeNombre", is("Hospital Notti")))
                .andReturn().getResponse().getContentAsString();

        Long turnoId = objectMapper.readTree(responseContent).get("id").asLong();

        // 2. Consultar Disponibilidad (GET 200 - es @Publico)
        mockMvc.perform(get("/api/v1/turnos/disponibilidad")
                        .param("especialidadId", especialidad.getId().toString())
                        .param("profesionalId", profesional.getId().toString())
                        .param("sedeId", sede.getId().toString())
                        .param("fecha", fechaFutura.toLocalDate().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(turnoId.intValue())));

        // 3. Cancelar Turno (PATCH 200)
        mockMvc.perform(patch("/api/v1/turnos/" + turnoId + "/cancelar")
                        .header("Authorization", "Bearer " + tokenPaciente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(turnoId.intValue())))
                .andExpect(jsonPath("$.estado", is("CANCELADO")));
    }

    @Test
    @DisplayName("Caso Datos Inválidos: Intento de reserva con campos nulos o fecha pasada debe retornar 400 Bad Request")
    void testReservaConDatosInvalidos() throws Exception {
        LocalDateTime fechaPasada = LocalDateTime.now().minusDays(1);

        ReservaTurnoRequestDTO request = new ReservaTurnoRequestDTO(
                null, // pacienteId nulo
                profesional.getId(),
                especialidad.getId(),
                sede.getId(),
                fechaPasada, // fecha en el pasado
                ""
        );

        mockMvc.perform(post("/api/v1/turnos/reserva")
                        .header("Authorization", "Bearer " + tokenPaciente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Datos de Solicitud Inválidos")))
                .andExpect(jsonPath("$.detalles.pacienteId", notNullValue()))
                .andExpect(jsonPath("$.detalles.fechaHora", is("La fecha y hora del turno deben ser en el futuro")));
    }

    @Test
    @DisplayName("Caso Conflicto de Horario: Intentar reservar dos turnos para el mismo profesional en el mismo horario debe retornar 409 Conflict")
    void testConflictoDeHorario() throws Exception {
        LocalDateTime fechaFutura = LocalDateTime.now().plusDays(3).withHour(11).withMinute(30).withSecond(0).withNano(0);

        ReservaTurnoRequestDTO request1 = new ReservaTurnoRequestDTO(
                paciente.getId(),
                profesional.getId(),
                especialidad.getId(),
                sede.getId(),
                fechaFutura,
                "Primera consulta"
        );

        // Primera reserva exitosa
        mockMvc.perform(post("/api/v1/turnos/reserva")
                        .header("Authorization", "Bearer " + tokenPaciente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        // Segunda reserva en el MISMO horario para el MISMO profesional
        ReservaTurnoRequestDTO request2 = new ReservaTurnoRequestDTO(
                paciente.getId(),
                profesional.getId(),
                especialidad.getId(),
                sede.getId(),
                fechaFutura, // Misma fechaHora
                "Segunda consulta solapada"
        );

        mockMvc.perform(post("/api/v1/turnos/reserva")
                        .header("Authorization", "Bearer " + tokenPaciente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Conflicto de Horario")))
                .andExpect(jsonPath("$.message", containsString("El profesional seleccionado ya posee un turno agendado")));
    }
}
