package ies.belgrano.turnos.controller;

import ies.belgrano.turnos.TurnosApplication;
import ies.belgrano.turnos.model.Especialidad;
import ies.belgrano.turnos.model.Profesional;
import ies.belgrano.turnos.repository.EspecialidadRepository;
import ies.belgrano.turnos.repository.ProfesionalRepository;
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
class EspecialidadControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private EspecialidadRepository especialidadRepository;

    @Autowired
    private ProfesionalRepository profesionalRepository;

    private MockMvc mockMvc;
    private Profesional medicoCardiologo;
    private Especialidad cardiologia;
    private Especialidad pediatria;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        cardiologia = especialidadRepository.save(new Especialidad("Cardiología Test", "Descripción cardiología"));
        pediatria = especialidadRepository.save(new Especialidad("Pediatría Test", "Descripción pediatría"));

        medicoCardiologo = new Profesional(null, "Roberto", "Pérez", "30999888", "MP-9988", "roberto@test.com", "261000111");
        medicoCardiologo.setEspecialidades(Set.of(cardiologia));
        medicoCardiologo = profesionalRepository.save(medicoCardiologo);
    }

    @Test
    @DisplayName("Debe listar todas las especialidades públicas si no se filtra por profesional")
    void debeListarTodasLasEspecialidades() throws Exception {
        mockMvc.perform(get("/api/v1/especialidades")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Debe listar únicamente las especialidades del profesional si se proporciona profesionalId")
    void debeListarEspecialidadesDelProfesional() throws Exception {
        mockMvc.perform(get("/api/v1/especialidades")
                        .param("profesionalId", medicoCardiologo.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre", is("Cardiología Test")));
    }

    @Test
    @DisplayName("Debe retornar 204 No Content si el profesional no tiene especialidades o no existe")
    void debeRetornar204SiProfesionalNoTieneEspecialidades() throws Exception {
        mockMvc.perform(get("/api/v1/especialidades")
                        .param("profesionalId", "999999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
