package ies.belgrano.turnos.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

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
import ies.belgrano.turnos.dto.GenerarSlotsRequestDTO;
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

@SpringBootTest(classes = TurnosApplication.class)
@TestPropertySource(properties = "spring.sql.init.mode=never")
@Transactional
class AgendaControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

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

    @Autowired
    private PacienteRepository pacienteRepository;

    private MockMvc mockMvc;
    private Profesional profesional1;
    private Profesional profesional2;
    private Especialidad especialidad;
    private Sede sede;
    private Paciente paciente;

    private String tokenAdmin;
    private String tokenMedico1;
    private String tokenMedico2;
    private String tokenPaciente;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        especialidad = especialidadRepository.save(new Especialidad("Oftalmología", "Cuidado ocular"));
        sede = sedeRepository.save(new Sede("Hospital Lagomaggiore", "Timoteo Gordillo s/n", "2614135500", "Capital"));

        profesional1 = profesionalRepository.save(new Profesional(
                null, "Lucía", "Herrera", "28111222", "M-11223", "lucia@hospital.gob.ar", "2614556677"
        ));
        profesional1.getEspecialidades().add(especialidad);

        profesional2 = profesionalRepository.save(new Profesional(
                null, "Gustavo", "Ríos", "29222333", "M-44556", "gustavo@hospital.gob.ar", "2614889900"
        ));
        profesional2.getEspecialidades().add(especialidad);

        // Usuario ADMIN
        Usuario adminUser = new Usuario("admin.agenda@saludmza.gob.ar", "pass", Rol.ADMINISTRADOR);
        usuarioRepository.save(adminUser);
        tokenAdmin = jwtService.generarToken(adminUser);

        // Usuario MEDICO 1 (Lucía Herrera)
        Usuario medico1User = new Usuario("lucia@hospital.gob.ar", "pass", Rol.MEDICO);
        medico1User.setProfesional(profesional1);
        usuarioRepository.save(medico1User);
        tokenMedico1 = jwtService.generarToken(medico1User);

        // Usuario MEDICO 2 (Gustavo Ríos)
        Usuario medico2User = new Usuario("gustavo@hospital.gob.ar", "pass", Rol.MEDICO);
        medico2User.setProfesional(profesional2);
        usuarioRepository.save(medico2User);
        tokenMedico2 = jwtService.generarToken(medico2User);

        // Usuario PACIENTE
        paciente = pacienteRepository.save(new Paciente(
                null, "Martín", "Palermo", "30111222", "martin@test.com", "2614998877", LocalDate.of(1985, 5, 20), null
        ));
        Usuario pacienteUser = new Usuario("paciente.test@gmail.com", "pass", Rol.PACIENTE);
        pacienteUser.setPaciente(paciente);
        usuarioRepository.save(pacienteUser);
        tokenPaciente = jwtService.generarToken(pacienteUser);
    }

    @Test
    @DisplayName("POST /api/v1/agenda/slots/generar - Admin genera slots exitosamente retorna 201 Created")
    void testGenerarSlots_Admin_Retorna201() throws Exception {
        LocalDate fechaLunes = LocalDate.now().plusWeeks(1);
        while (fechaLunes.getDayOfWeek() != DayOfWeek.MONDAY) {
            fechaLunes = fechaLunes.plusDays(1);
        }

        GenerarSlotsRequestDTO request = new GenerarSlotsRequestDTO(
                profesional1.getId(),
                especialidad.getId(),
                sede.getId(),
                fechaLunes,
                fechaLunes,
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                20, // 3 slots: 09:00-09:20, 09:20-09:40, 09:40-10:00
                List.of(DayOfWeek.MONDAY)
        );

        mockMvc.perform(post("/api/v1/agenda/slots/generar")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cantidadSlotsGenerados", is(3)))
                .andExpect(jsonPath("$.profesionalId", is(profesional1.getId().intValue())))
                .andExpect(jsonPath("$.slots", hasSize(3)))
                .andExpect(jsonPath("$.slots[0].estado", is("DISPONIBLE")));
    }

    @Test
    @DisplayName("POST /api/v1/agenda/slots/generar - Médico genera slots propios exitosamente retorna 201 Created")
    void testGenerarSlots_MedicoPropio_Retorna201() throws Exception {
        LocalDate fechaMartes = LocalDate.now().plusWeeks(1);
        while (fechaMartes.getDayOfWeek() != DayOfWeek.TUESDAY) {
            fechaMartes = fechaMartes.plusDays(1);
        }

        GenerarSlotsRequestDTO request = new GenerarSlotsRequestDTO(
                profesional1.getId(),
                especialidad.getId(),
                sede.getId(),
                fechaMartes,
                fechaMartes,
                LocalTime.of(14, 0),
                LocalTime.of(15, 0),
                30, // 2 slots: 14:00-14:30, 14:30-15:00
                List.of(DayOfWeek.TUESDAY)
        );

        mockMvc.perform(post("/api/v1/agenda/slots/generar")
                        .header("Authorization", "Bearer " + tokenMedico1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cantidadSlotsGenerados", is(2)))
                .andExpect(jsonPath("$.profesionalId", is(profesional1.getId().intValue())));
    }

    @Test
    @DisplayName("POST /api/v1/agenda/slots/generar - Médico intenta generar slots de otro médico retorna 403 Forbidden")
    void testGenerarSlots_MedicoOtroProfesional_Retorna403() throws Exception {
        LocalDate fecha = LocalDate.now().plusWeeks(1);

        GenerarSlotsRequestDTO request = new GenerarSlotsRequestDTO(
                profesional2.getId(), // Intenta generar para profesional2 usando token de medico1
                especialidad.getId(),
                sede.getId(),
                fecha,
                fecha,
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                30,
                null
        );

        mockMvc.perform(post("/api/v1/agenda/slots/generar")
                        .header("Authorization", "Bearer " + tokenMedico1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    @Test
    @DisplayName("POST /api/v1/agenda/slots/generar - Paciente intenta generar slots retorna 403 Forbidden")
    void testGenerarSlots_Paciente_Retorna403() throws Exception {
        LocalDate fecha = LocalDate.now().plusWeeks(1);

        GenerarSlotsRequestDTO request = new GenerarSlotsRequestDTO(
                profesional1.getId(),
                especialidad.getId(),
                sede.getId(),
                fecha,
                fecha,
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                30,
                null
        );

        mockMvc.perform(post("/api/v1/agenda/slots/generar")
                        .header("Authorization", "Bearer " + tokenPaciente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    @Test
    @DisplayName("POST /api/v1/agenda/slots/generar - Solapamiento con turno existente retorna 409 Conflict")
    void testGenerarSlots_ConflictoSolapamiento_Retorna409() throws Exception {
        LocalDate fechaLunes = LocalDate.now().plusWeeks(1);
        while (fechaLunes.getDayOfWeek() != DayOfWeek.MONDAY) {
            fechaLunes = fechaLunes.plusDays(1);
        }

        LocalDateTime inicio = fechaLunes.atTime(8, 30);
        LocalDateTime fin = fechaLunes.atTime(9, 15);

        // Turno previo activo
        turnoRepository.save(new Turno(
                null, inicio, fin, EstadoTurno.RESERVADO, null, profesional1, especialidad, sede, null
        ));

        // Intenta generar slots que caen entre 08:00 y 10:00 (solapándose con 08:30-09:15)
        GenerarSlotsRequestDTO request = new GenerarSlotsRequestDTO(
                profesional1.getId(),
                especialidad.getId(),
                sede.getId(),
                fechaLunes,
                fechaLunes,
                LocalTime.of(8, 0),
                LocalTime.of(10, 0),
                30,
                List.of(DayOfWeek.MONDAY)
        );

        mockMvc.perform(post("/api/v1/agenda/slots/generar")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Conflicto de Horario")));
    }

    @Test
    @DisplayName("DELETE /api/v1/agenda/slots/{id} - Admin elimina slot DISPONIBLE retorna 204 No Content")
    void testEliminarSlot_Disponible_Admin_Retorna204() throws Exception {
        LocalDateTime inicio = LocalDateTime.now().plusDays(2).withHour(9).withMinute(0);
        Turno slot = turnoRepository.save(new Turno(
                null, inicio, inicio.plusMinutes(20), EstadoTurno.DISPONIBLE, null, profesional1, especialidad, sede, null
        ));

        mockMvc.perform(delete("/api/v1/agenda/slots/" + slot.getId())
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/agenda/slots/{id} - Intento de eliminar slot RESERVADO retorna 409 Conflict")
    void testEliminarSlot_Reservado_Retorna409() throws Exception {
        LocalDateTime inicio = LocalDateTime.now().plusDays(2).withHour(11).withMinute(0);
        Turno slot = turnoRepository.save(new Turno(
                null, inicio, inicio.plusMinutes(30), EstadoTurno.RESERVADO, null, profesional1, especialidad, sede, null
        ));

        mockMvc.perform(delete("/api/v1/agenda/slots/" + slot.getId())
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));
    }

    @Test
    @DisplayName("DELETE /api/v1/agenda/slots/{id} - Médico intenta eliminar slot de otro médico retorna 403 Forbidden")
    void testEliminarSlot_MedicoOtroProfesional_Retorna403() throws Exception {
        LocalDateTime inicio = LocalDateTime.now().plusDays(2).withHour(16).withMinute(0);
        Turno slot = turnoRepository.save(new Turno(
                null, inicio, inicio.plusMinutes(30), EstadoTurno.DISPONIBLE, null, profesional2, especialidad, sede, null
        ));

        // Intenta borrar slot de profesional2 usando token de medico1
        mockMvc.perform(delete("/api/v1/agenda/slots/" + slot.getId())
                        .header("Authorization", "Bearer " + tokenMedico1))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    @Test
    @DisplayName("DELETE /api/v1/agenda/slots/{id} - Slot inexistente retorna 404 Not Found")
    void testEliminarSlot_NoExiste_Retorna404() throws Exception {
        mockMvc.perform(delete("/api/v1/agenda/slots/999999")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("GET /api/v1/agenda - Médico consulta su agenda y visualiza slots DISPONIBLE y RESERVADO con datos del paciente")
    void testConsultarAgenda_MedicoExitoso() throws Exception {
        LocalDateTime inicio1 = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0);
        LocalDateTime inicio2 = LocalDateTime.now().plusDays(1).withHour(9).withMinute(30);

        // Slot 1: DISPONIBLE
        turnoRepository.save(new Turno(
                null, inicio1, inicio1.plusMinutes(30), EstadoTurno.DISPONIBLE, null, profesional1, especialidad, sede, null
        ));

        // Slot 2: RESERVADO con paciente
        Turno reservado = new Turno(
                null, inicio2, inicio2.plusMinutes(30), EstadoTurno.RESERVADO, paciente, profesional1, especialidad, sede, "Control pediátrico"
        );
        turnoRepository.save(reservado);

        mockMvc.perform(get("/api/v1/agenda")
                        .header("Authorization", "Bearer " + tokenMedico1)
                        .param("especialidadId", String.valueOf(especialidad.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].estado", is("DISPONIBLE")))
                .andExpect(jsonPath("$[1].estado", is("RESERVADO")))
                .andExpect(jsonPath("$[1].pacienteNombreCompleto", is(paciente.getNombre() + " " + paciente.getApellido())))
                .andExpect(jsonPath("$[1].motivoConsulta", is("Control pediátrico")));
    }

    @Test
    @DisplayName("GET /api/v1/agenda - Paciente intenta consultar agenda médica retorna 403 Forbidden")
    void testConsultarAgenda_Paciente_Retorna403() throws Exception {
        mockMvc.perform(get("/api/v1/agenda")
                        .header("Authorization", "Bearer " + tokenPaciente))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }
}

