package ies.belgrano.turnos.service;

import ies.belgrano.turnos.dto.PacienteResponseDTO;
import ies.belgrano.turnos.dto.TurnoResponseDTO;
import ies.belgrano.turnos.model.Paciente;

import java.util.List;

public interface PacienteService {
    List<TurnoResponseDTO> obtenerTurnosPorPaciente(Long pacienteId);
    default List<TurnoResponseDTO> listarTurnosPorPaciente(Long pacienteId) {
        return obtenerTurnosPorPaciente(pacienteId);
    }
    PacienteResponseDTO buscarPorDni(String dni);
    List<PacienteResponseDTO> listarPacientes(String filtro);

    //Métodos Grupo Familiar 
    
    // Registrar un nuevo dependiente/familiar asociado a un tutor
    PacienteResponseDTO registrarFamiliar(Long tutorId, Paciente familiarDto); 
    
    // Listar únicamente los familiares activos (isActive = true) de un tutor
    List<PacienteResponseDTO> listarFamiliaresActivos(Long tutorId);
    
    // Aplicar baja lógica a un familiar (isActive = false) validando turnos futuros
    void bajaLogicaFamiliar(Long tutorId, Long familiarId);
    
    // Reactivar a un familiar (isActive = true)
    void reactivarFamiliar(Long tutorId, Long familiarId);

}