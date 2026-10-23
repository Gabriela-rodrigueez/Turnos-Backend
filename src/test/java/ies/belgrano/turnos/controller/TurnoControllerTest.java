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
import ies.belgrano.turnos.dto.CambiarEstadoTurnoRequestDTO;
import ies.belgrano.turnos.dto.ReservaTurnoRequestDTO;
import ies.belgrano.turnos.model.Especialidad;
import ies.belgrano.turnos.model.EstadoTurno;
import ies.belgrano.turnos.model.Paciente;
import ies.belgrano.turnos.model.Profesional;
import ies.belgrano.turnos.model.Rol;
import ies.belgrano.turnos.model.Sede;
import ies.belgrano.turnos.model.Turno;
import ies.belgrano.turnos.model.Usuario;
import ies.belgrano.turnos.repository.EspecialidadRepository;
import ies.belgrano.turnos.repository.PacienteRepository;
import ies.belgrano.turnos.repository.ProfesionalRepository;
import ies.belgrano.turnos.repository.SedeRepository;
import ies.belgrano.turnos.repository.TurnoRepository;
import ies.belgrano.turnos.repository.UsuarioRepository;
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

    @Autowired
    private TurnoRepository turnoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private MockMvc mockMvc;
    private Paciente paciente;
    private Profesional profesional;
    private Profesional profesional2;
    private Especialidad especialidad;
    private Sede sede;
    private String tokenPaciente;
    private String tokenAdmin;
    private String tokenMedico;
    private String tokenMedico2;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        paciente = pacienteRepository.save(new Paciente(null, "Carlos", "Gómez", "30111222", "carlos@gmail.com", "261444333", LocalDate.of(1985, 3, 20), null));
        especialidad = especialidadRepository.save(new Especialidad("Cardiología", "Estudio del corazón"));
        profesional = profesionalRepository.save(new Profesional(null, "Roberto", "Pérez", "20999888", "M-12345", "roberto@hospital.com", "261555666"));
        profesional.getEspecialidades().add(especialidad);
        profesional2 = profesionalRepository.save(new Profesional(null, "Laura", "Méndez", "27888999", "M-67890", "laura@hospital.com", "261555777"));
        profesional2.getEspecialidades().add(especialidad);
        sede = sedeRepository.save(new Sede("Hospital Notti", "Bandera de los Andes 2603", "2614132000", "Guaymallén"));

        Usuario usuario = new Usuario("carlos@gmail.com", "hash", Rol.PACIENTE);
        usuario.setPaciente(paciente);
        usuarioRepository.save(usuario);
        tokenPaciente = jwtService.generarToken(usuario);

        Usuario admin = new Usuario("admin.turnos@saludmza.gob.ar", "hash", Rol.ADMINISTRADOR);
        usuarioRepository.save(admin);
        tokenAdmin = jwtService.generarToken(admin);

        Usuario medico = new Usuario("roberto@hospital.com", "hash", Rol.MEDICO);
        medico.setProfesional(profesional);
        usuarioRepository.save(medico);
        tokenMedico = jwtService.generarToken(medico);

        Usuario medico2 = new Usuario("laura@hospital.com", "hash", Rol.MEDICO);
        medico2.setProfesional(profesional2);
        usuarioRepository.save(medico2);
        tokenMedico2 = jwtService.generarToken(medico2);
    }

    @Test
    @DisplayName("Caso Exitoso: Reservar turno, consultar disponibilidad y cancelar turno")
    void testFlujoExitosoReservaDisponibilidadYCancelacion() throws Exception {
        LocalDateTime fechaFutura = LocalDateTime.now().plusDays(5).withHour(10).withMinute(0).withSecond(0).withNano(0);
        Turno slot = turnoRepository.save(new Turno(null, fechaFutura, fechaFutura.plusMinutes(30), EstadoTurno.DISPONIBLE, null, profesional, especialidad, sede, null));

        // 1. Consultar Disponibilidad previa: el slot debe figurar como DISPONIBLE
        mockMvc.perform(get("/api/v1/turnos/disponibilidad")
                        .param("especialidadId", especialidad.getId().toString())
                        .param("profesionalId", profesional.getId().toString())
                        .param("sedeId", sede.getId().toString())
                        .param("fecha", fechaFutura.toLocalDate().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(slot.getId().intValue())));

        ReservaTurnoRequestDTO request = new ReservaTurnoRequestDTO(
                slot.getId(),
                paciente.getId(),
                "Control de presión arterial"
        );

        // 2. Reservar Turno (POST 201)
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

        // 3. Consultar Disponibilidad posterior: el slot ya NO debe figurar porque fue RESERVADO
        mockMvc.perform(get("/api/v1/turnos/disponibilidad")
                        .param("especialidadId", especialidad.getId().toString())
                        .param("profesionalId", profesional.getId().toString())
                        .param("sedeId", sede.getId().toString())
                        .param("fecha", fechaFutura.toLocalDate().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // 4. Cancelar Turno (PATCH 200)
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
        Turno slot = turnoRepository.save(new Turno(null, fechaFutura, fechaFutura.plusMinutes(30), EstadoTurno.DISPONIBLE, null, profesional, especialidad, sede, null));

        ReservaTurnoRequestDTO request1 = new ReservaTurnoRequestDTO(
                slot.getId(),
                paciente.getId(),
                "Primera consulta"
        );

        // Primera reserva exitosa
        mockMvc.perform(post("/api/v1/turnos/reserva")
                        .header("Authorization", "Bearer " + tokenPaciente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        // Segunda reserva en el MISMO slot
        ReservaTurnoRequestDTO request2 = new ReservaTurnoRequestDTO(
                slot.getId(),
                paciente.getId(),
                "Segunda consulta solapada"
        );

        mockMvc.perform(post("/api/v1/turnos/reserva")
                        .header("Authorization", "Bearer " + tokenPaciente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));
    }

    @Test
    @DisplayName("PATCH /api/v1/turnos/{id}/estado - Admin cambia estado a ASISTIDO retorna 200 OK")
    void testCambiarEstado_Admin_Exitoso() throws Exception {
        LocalDateTime fecha = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0);
        Turno turno = turnoRepository.save(new Turno(null, fecha, fecha.plusMinutes(30), EstadoTurno.RESERVADO, paciente, profesional, especialidad, sede, null));

        CambiarEstadoTurnoRequestDTO request = new CambiarEstadoTurnoRequestDTO(EstadoTurno.ASISTIDO, "Consulta realizada");

        mockMvc.perform(patch("/api/v1/turnos/" + turno.getId() + "/estado")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(turno.getId().intValue())))
                .andExpect(jsonPath("$.estado", is("ASISTIDO")));
    }

    @Test
    @DisplayName("PATCH /api/v1/turnos/{id}/estado - Médico cambia estado de su propio turno retorna 200 OK")
    void testCambiarEstado_MedicoPropio_Exitoso() throws Exception {
        LocalDateTime fecha = LocalDateTime.now().plusDays(2).withHour(11).withMinute(0);
        Turno turno = turnoRepository.save(new Turno(null, fecha, fecha.plusMinutes(30), EstadoTurno.RESERVADO, paciente, profesional, especialidad, sede, null));

        CambiarEstadoTurnoRequestDTO request = new CambiarEstadoTurnoRequestDTO(EstadoTurno.CANCELADO, "Cancelado por el médico");

        mockMvc.perform(patch("/api/v1/turnos/" + turno.getId() + "/estado")
                        .header("Authorization", "Bearer " + tokenMedico)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(turno.getId().intValue())))
                .andExpect(jsonPath("$.estado", is("CANCELADO")));
    }

    @Test
    @DisplayName("PATCH /api/v1/turnos/{id}/estado - Médico intenta modificar turno de otro médico retorna 403 Forbidden")
    void testCambiarEstado_MedicoOtro_Retorna403() throws Exception {
        LocalDateTime fecha = LocalDateTime.now().plusDays(2).withHour(12).withMinute(0);
        Turno turnoDeProfesional1 = turnoRepository.save(new Turno(null, fecha, fecha.plusMinutes(30), EstadoTurno.RESERVADO, paciente, profesional, especialidad, sede, null));

        CambiarEstadoTurnoRequestDTO request = new CambiarEstadoTurnoRequestDTO(EstadoTurno.ASISTIDO, "Intento ilegítimo");

        // Intentar modificar usando tokenMedico2 (Laura Méndez) sobre turno de profesional1 (Roberto Pérez)
        mockMvc.perform(patch("/api/v1/turnos/" + turnoDeProfesional1.getId() + "/estado")
                        .header("Authorization", "Bearer " + tokenMedico2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    @Test
    @DisplayName("PATCH /api/v1/turnos/{id}/estado - Paciente intenta modificar estado retorna 403 Forbidden")
    void testCambiarEstado_Paciente_Retorna403() throws Exception {
        LocalDateTime fecha = LocalDateTime.now().plusDays(2).withHour(14).withMinute(0);
        Turno turno = turnoRepository.save(new Turno(null, fecha, fecha.plusMinutes(30), EstadoTurno.RESERVADO, paciente, profesional, especialidad, sede, null));

        CambiarEstadoTurnoRequestDTO request = new CambiarEstadoTurnoRequestDTO(EstadoTurno.ASISTIDO, "Nota");

        mockMvc.perform(patch("/api/v1/turnos/" + turno.getId() + "/estado")
                        .header("Authorization", "Bearer " + tokenPaciente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    @Test
    @DisplayName("PATCH /api/v1/turnos/{id}/estado - Estado destino no permitido (DISPONIBLE) retorna 409 Conflict")
    void testCambiarEstado_EstadoDestinoInvalido_Retorna409() throws Exception {
        LocalDateTime fecha = LocalDateTime.now().plusDays(2).withHour(15).withMinute(0);
        Turno turno = turnoRepository.save(new Turno(null, fecha, fecha.plusMinutes(30), EstadoTurno.RESERVADO, paciente, profesional, especialidad, sede, null));

        CambiarEstadoTurnoRequestDTO request = new CambiarEstadoTurnoRequestDTO(EstadoTurno.DISPONIBLE, "Revertir a disponible no permitido por este endpoint");

        mockMvc.perform(patch("/api/v1/turnos/" + turno.getId() + "/estado")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));
    }

    @Test
    @DisplayName("GET /api/v1/turnos/disponibilidad - No retorna turnos expirados o pasados")
    void testConsultarDisponibilidad_ExcluyeTurnosExpirados() throws Exception {
        LocalDateTime fechaPasada = LocalDateTime.now().minusDays(2).withHour(10).withMinute(0);
        LocalDateTime fechaFutura = LocalDateTime.now().plusDays(3).withHour(10).withMinute(0);

        Turno turnoExpirado = turnoRepository.save(new Turno(null, fechaPasada, fechaPasada.plusMinutes(30), EstadoTurno.DISPONIBLE, null, profesional, especialidad, sede, null));
        Turno turnoFuturo = turnoRepository.save(new Turno(null, fechaFutura, fechaFutura.plusMinutes(30), EstadoTurno.DISPONIBLE, null, profesional, especialidad, sede, null));

        // Consulta general sin fecha: solo debe venir el turno futuro
        mockMvc.perform(get("/api/v1/turnos/disponibilidad")
                        .param("especialidadId", especialidad.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(turnoFuturo.getId().intValue())));

        // Consulta filtrando por fecha pasada: debe retornar lista vacía
        mockMvc.perform(get("/api/v1/turnos/disponibilidad")
                        .param("especialidadId", especialidad.getId().toString())
                        .param("fecha", fechaPasada.toLocalDate().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("POST /api/v1/turnos/reserva - Intento de reservar turno expirado por ID retorna 409 Conflict")
    void testReservaTurno_TurnoExpirado_Retorna409Conflict() throws Exception {
        LocalDateTime fechaPasada = LocalDateTime.now().minusDays(1).withHour(10).withMinute(0);
        Turno turnoExpirado = turnoRepository.save(new Turno(null, fechaPasada, fechaPasada.plusMinutes(30), EstadoTurno.DISPONIBLE, null, profesional, especialidad, sede, null));

        ReservaTurnoRequestDTO request = new ReservaTurnoRequestDTO(
                turnoExpirado.getId(),
                paciente.getId(),
                "Intento de reserva retroactiva"
        );

        mockMvc.perform(post("/api/v1/turnos/reserva")
                        .header("Authorization", "Bearer " + tokenPaciente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", containsString("expirado")));
    }
}
