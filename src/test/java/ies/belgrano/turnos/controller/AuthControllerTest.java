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
import ies.belgrano.turnos.repository.ObraSocialRepository;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest(classes = TurnosApplication.class)
@TestPropertySource(properties = "spring.sql.init.mode=never")
@Transactional
class AuthControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObraSocialRepository obraSocialRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

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

        // 2. Login (POST 200)
        LoginRequestDTO loginReq = new LoginRequestDTO("nuevo.paciente@gmail.com", "Password123");
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
    @DisplayName("Caso 401 Unauthorized: Solicitud sin cabecera Authorization a endpoint privado /me")
    void testAccesoSinTokenRetorna401() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("No Autorizado")));
    }
}
