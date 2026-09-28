package ies.belgrano.turnos.controller;

import ies.belgrano.turnos.TurnosApplication;
import ies.belgrano.turnos.model.*;
import ies.belgrano.turnos.repository.*;
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

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = TurnosApplication.class)
@TestPropertySource(properties = "spring.sql.init.mode=never")
@Transactional
class PacienteControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

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

    private MockMvc mockMvc;
    private Paciente pacienteConTurnos;
    private Paciente pacienteSinTurnos;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        pacienteConTurnos = pacienteRepository.save(new Paciente(null, "Juan", "González", "35111222", "juan@gmail.com", "2614001111", LocalDate.of(1990, 5, 15), null));
        pacienteSinTurnos = pacienteRepository.save(new Paciente(null, "Lucía", "Martínez", "40222333", "lucia@hotmail.com", "2614002222", LocalDate.of(1997, 11, 20), null));

        Especialidad especialidad = especialidadRepository.save(new Especialidad("Cardiología", "Estudio del corazón"));
        Profesional profesional = profesionalRepository.save(new Profesional(null, "Roberto", "Pérez", "20999888", "M-12345", "roberto@hospital.com", "261555666"));
        Sede sede = sedeRepository.save(new Sede("Hospital Central", "Alem 450", "2614490000", "Mendoza"));

        Turno turno = new Turno(null, LocalDateTime.now().plusDays(2), EstadoTurno.RESERVADO, pacienteConTurnos, profesional, especialidad, sede, "Control periódico");
        turnoRepository.save(turno);
    }

    @Test
    @DisplayName("Caso 200 OK: Si el paciente tiene turnos registrados, retorna 200 OK con la lista de DTOs")
    void testObtenerTurnos_ConTurnos_Retorna200Ok() throws Exception {
        mockMvc.perform(get("/api/v1/pacientes/{pacienteId}/turnos", pacienteConTurnos.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].pacienteId", is(pacienteConTurnos.getId().intValue())))
                .andExpect(jsonPath("$[0].pacienteNombreCompleto", is("Juan González")))
                .andExpect(jsonPath("$[0].estado", is("RESERVADO")))
                .andExpect(jsonPath("$[0].motivoConsulta", is("Control periódico")));
    }

    @Test
    @DisplayName("Caso 204 No Content: Si el paciente existe pero no tiene turnos agendados, retorna 204 No Content")
    void testObtenerTurnos_SinTurnos_Retorna204NoContent() throws Exception {
        mockMvc.perform(get("/api/v1/pacientes/{pacienteId}/turnos", pacienteSinTurnos.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("Caso 404 Not Found: Si el paciente ID no existe en la base de datos, retorna 404 Not Found")
    void testObtenerTurnos_PacienteInexistente_Retorna404NotFound() throws Exception {
        Long pacienteIdInexistente = 99999L;

        mockMvc.perform(get("/api/v1/pacientes/{pacienteId}/turnos", pacienteIdInexistente)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Recurso No Encontrado")))
                .andExpect(jsonPath("$.message", containsString("No se encontró el paciente con ID: " + pacienteIdInexistente)));
    }
}