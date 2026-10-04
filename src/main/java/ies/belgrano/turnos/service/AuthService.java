package ies.belgrano.turnos.service;

import ies.belgrano.turnos.dto.LoginRequestDTO;
import ies.belgrano.turnos.dto.LoginResponseDTO;
import ies.belgrano.turnos.dto.RegistroRequestDTO;

public interface AuthService {
    LoginResponseDTO registro(RegistroRequestDTO dto);

    LoginResponseDTO login(LoginRequestDTO dto);

    LoginResponseDTO obtenerUsuarioActual(Long usuarioId);
}
