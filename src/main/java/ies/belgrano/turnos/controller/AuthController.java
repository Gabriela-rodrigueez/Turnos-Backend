package ies.belgrano.turnos.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ies.belgrano.turnos.dto.ErrorResponseDTO;
import ies.belgrano.turnos.dto.LoginRequestDTO;
import ies.belgrano.turnos.dto.LoginResponseDTO;
import ies.belgrano.turnos.dto.RegistroRequestDTO;
import ies.belgrano.turnos.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "API REST para Registro, Login y Gestión de Sesiones JWT")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro")
    @Operation(summary = "Registrar nuevo paciente", description = "Crea la cuenta de usuario y perfil de un paciente, retornando el token JWT inicial.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Paciente registrado exitosamente",
                    content = @Content(schema = @Schema(implementation = LoginResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de registro inválidos (email, DNI o formato)",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflicto: El DNI o Email ya se encuentran registrados",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<LoginResponseDTO> registro(@Valid @RequestBody RegistroRequestDTO request) {
        LoginResponseDTO response = authService.registro(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica al usuario por correo electrónico y contraseña, devolviendo el token JWT firmado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inicio de sesión exitoso",
                    content = @Content(schema = @Schema(implementation = LoginResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Formato de datos de login inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas o cuenta deshabilitada",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener perfil del usuario autenticado", description = "Retorna la información del usuario en sesión utilizando el token JWT del header Authorization.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Datos de perfil obtenidos con éxito",
                    content = @Content(schema = @Schema(implementation = LoginResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente, no válido o expirado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<LoginResponseDTO> obtenerPerfil(HttpServletRequest request) {
        Long usuarioId = (Long) request.getAttribute("usuarioId");
        LoginResponseDTO response = authService.obtenerUsuarioActual(usuarioId);
        return ResponseEntity.ok(response);
    }
}
