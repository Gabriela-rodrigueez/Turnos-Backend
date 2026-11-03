package ies.belgrano.turnos.controller;

import ies.belgrano.turnos.TurnosApplication;
import ies.belgrano.turnos.model.Especialidad;
import ies.belgrano.turnos.model.EstadoTurno;
import ies.belgrano.turnos.model.Profesional;
import ies.belgrano.turnos.model.Sede;
import ies.belgrano.turnos.model.Turno;
import ies.belgrano.turnos.repository.EspecialidadRepository;
import ies.belgrano.turnos.repository.ProfesionalRepository;
import ies.belgrano.turnos.repository.SedeRepository;
import ies.belgrano.turnos.repository.TurnoRepository;
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

import java.time.LocalDateTime;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TurnosApplication.class)
@TestPropertySource(properties = "spring.sql.init.mode=never")
@Transactional
class SedeControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private SedeRepository sedeRepository;

    @Autowired
    private ProfesionalRepository profesionalRepository;

    @Autowired
    private EspecialidadRepository especialidadRepository;

    @Autowired
    private TurnoRepository turnoRepository;

    private MockMvc mockMvc;
    private Profesional profesional;
    private Sede sedeCentral;
    private Sede sedeNotti;
    private Especialidad especialidad;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        sedeCentral = sedeRepository.save(new Sede("Hospital Central Test", "Alem 450", "2614490000", "Mendoza"));
        sedeNotti = sedeRepository.save(new Sede("Hospital Notti Test", "Bandera de los Andes", "2614132000", "Guaymallén"));

        especialidad = especialidadRepository.save(new Especialidad("Clínica Médica Sede Test", "Atención general"));

        profesional = new Profesional(null, "Roberto", "Pérez", "31111222", "M-TEST-12", "roberto.sede@hospital.com", "2615556661");
        profesional.setEspecialidades(Set.of(especialidad));
        profesional = profesionalRepository.save(profesional);

        // Turno asociado al profesional en la Sede Central
        LocalDateTime ahora = LocalDateTime.now().plusDays(2);
        Turno turno = new Turno(null, ahora, ahora.plusMinutes(30), EstadoTurno.DISPONIBLE, null, profesional, especialidad, sedeCentral, null);
        turnoRepository.save(turno);
    }

    @Test
    @DisplayName("Debe listar todas las sedes hospitalarias")
    void debeListarTodasLasSedes() throws Exception {
        mockMvc.perform(get("/api/v1/sedes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Debe listar las sedes asociadas al profesional médico según su agenda")
    void debeListarSedesPorProfesional() throws Exception {
        mockMvc.perform(get("/api/v1/sedes/profesional/{profesionalId}", profesional.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre", is("Hospital Central Test")));
    }

    @Test
    @DisplayName("Debe retornar 204 No Content si el profesional no tiene sedes con agenda")
    void debeRetornar204SiProfesionalNoTieneSedes() throws Exception {
        mockMvc.perform(get("/api/v1/sedes/profesional/{profesionalId}", 999999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
