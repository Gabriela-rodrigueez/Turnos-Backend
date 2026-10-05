package ies.belgrano.turnos.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

import ies.belgrano.turnos.TurnosApplication;
import ies.belgrano.turnos.model.ObraSocial;
import ies.belgrano.turnos.model.Paciente;
import ies.belgrano.turnos.model.Rol;
import ies.belgrano.turnos.model.Usuario;
import ies.belgrano.turnos.repository.ObraSocialRepository;
import ies.belgrano.turnos.repository.PacienteRepository;
import ies.belgrano.turnos.repository.UsuarioRepository;
import ies.belgrano.turnos.security.JwtService;

@SpringBootTest(classes = TurnosApplication.class)
@TestPropertySource(properties = "spring.sql.init.mode=never")
@Transactional
class AdminPacienteControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ObraSocialRepository obraSocialRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private MockMvc mockMvc;
    private Paciente paciente1;
    private Paciente paciente2;
    private String tokenAdmin;
    private String tokenPaciente;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        ObraSocial osep = obraSocialRepository.save(new ObraSocial("OSEP Mendoza", "OSEP-001"));
        ObraSocial pami = obraSocialRepository.save(new ObraSocial("PAMI", "PAMI-002"));

        paciente1 = pacienteRepository.save(new Paciente(
                null, "Juan", "González", "35111222", "juan.gonzalez@hospital.com",
                "2614001111", LocalDate.of(1990, 5, 15), osep
        ));

        paciente2 = pacienteRepository.save(new Paciente(
                null, "Lucía", "Martínez", "40222333", "lucia.martinez@hospital.com",
                "2614002222", LocalDate.of(1997, 11, 20), pami
        ));

        Usuario admin = new Usuario("admin@saludmza.gob.ar", "hashPassword", Rol.ADMINISTRADOR);
        usuarioRepository.save(admin);
        tokenAdmin = jwtService.generarToken(admin);

        Usuario pacienteUser = new Usuario("juan.gonzalez@hospital.com", "hashPassword", Rol.PACIENTE);
        pacienteUser.setPaciente(paciente1);
        usuarioRepository.save(pacienteUser);
        tokenPaciente = jwtService.generarToken(pacienteUser);
    }

    @Test
    @DisplayName("GET /api/v1/admin/pacientes/buscar?dni={dni} - Retorna 200 OK con el DTO del paciente encontrado")
    void testBuscarPacientePorDni_Exitoso_Retorna200Ok() throws Exception {
        mockMvc.perform(get("/api/v1/admin/pacientes/buscar")
                        .param("dni", "35111222")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(paciente1.getId().intValue())))
                .andExpect(jsonPath("$.nombre", is("Juan")))
                .andExpect(jsonPath("$.apellido", is("González")))
                .andExpect(jsonPath("$.dni", is("35111222")))
                .andExpect(jsonPath("$.email", is("juan.gonzalez@hospital.com")))
                .andExpect(jsonPath("$.obraSocialNombre", is("OSEP Mendoza")));
    }

    @Test
    @DisplayName("GET /api/v1/admin/pacientes/buscar?dni={dni} - Retorna 404 Not Found si no está registrado")
    void testBuscarPacientePorDni_NoExiste_Retorna404NotFound() throws Exception {
        String dniInexistente = "99888777";

        mockMvc.perform(get("/api/v1/admin/pacientes/buscar")
                        .param("dni", dniInexistente)
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Recurso No Encontrado")))
                .andExpect(jsonPath("$.message", containsString("No se encontró ningún paciente con el DNI: " + dniInexistente)));
    }

    @Test
    @DisplayName("GET /api/v1/admin/pacientes/buscar - Retorna 403 Forbidden cuando el rol no es ADMINISTRADOR")
    void testBuscarPacientePorDni_AccesoDenegadoParaPaciente_Retorna403Forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/pacientes/buscar")
                        .param("dni", "35111222")
                        .header("Authorization", "Bearer " + tokenPaciente)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("Acceso Denegado")));
    }

    @Test
    @DisplayName("GET /api/v1/admin/pacientes/buscar - Retorna 401 Unauthorized sin token JWT")
    void testBuscarPacientePorDni_SinAutenticacion_Retorna401Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/admin/pacientes/buscar")
                        .param("dni", "35111222")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("No Autorizado")));
    }

    @Test
    @DisplayName("GET /api/v1/admin/pacientes - Lista todos los pacientes registrados")
    void testListarPacientes_SinFiltro_RetornaTodosLosPacientes() throws Exception {
        mockMvc.perform(get("/api/v1/admin/pacientes")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].dni", is("35111222")))
                .andExpect(jsonPath("$[1].dni", is("40222333")));
    }

    @Test
    @DisplayName("GET /api/v1/admin/pacientes?filtro={filtro} - Filtra por apellido correctamente")
    void testListarPacientes_ConFiltroPorApellido_RetornaFiltrados() throws Exception {
        mockMvc.perform(get("/api/v1/admin/pacientes")
                        .param("filtro", "González")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre", is("Juan")))
                .andExpect(jsonPath("$[0].apellido", is("González")));
    }

    @Test
    @DisplayName("GET /api/v1/admin/pacientes?buscar={buscar} - Filtra por nombre correctamente")
    void testListarPacientes_ConFiltroPorNombre_RetornaFiltrados() throws Exception {
        mockMvc.perform(get("/api/v1/admin/pacientes")
                        .param("buscar", "lucia")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre", is("Lucía")))
                .andExpect(jsonPath("$[0].apellido", is("Martínez")));
    }

    @Test
    @DisplayName("GET /api/v1/admin/pacientes - Retorna 403 Forbidden si el rol es PACIENTE")
    void testListarPacientes_AccesoDenegadoParaPaciente_Retorna403Forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/pacientes")
                        .header("Authorization", "Bearer " + tokenPaciente)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }
}
