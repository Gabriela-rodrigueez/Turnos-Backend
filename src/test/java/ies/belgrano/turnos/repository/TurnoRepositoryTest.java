package ies.belgrano.turnos.repository;

import ies.belgrano.turnos.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class TurnoRepositoryTest {

    @Autowired
    private TurnoRepository turnoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ProfesionalRepository profesionalRepository;

    @Autowired
    private EspecialidadRepository especialidadRepository;

    @Autowired
    private SedeRepository sedeRepository;

    @Autowired
    private ObraSocialRepository obraSocialRepository;

    @Test
    @DisplayName("Debe guardar y recuperar un turno con sus entidades asociadas")
    void debeGuardarYRecuperarTurno() {
        ObraSocial osep = obraSocialRepository.save(new ObraSocial("OSEP", "OSEP-MZA"));
        
        Paciente paciente = new Paciente(null, "Juan", "Pérez", "12345678", "juan@email.com", "261111222", LocalDate.of(1990, 5, 15), osep);
        paciente = pacienteRepository.save(paciente);

        Especialidad pediatria = especialidadRepository.save(new Especialidad("Pediatría", "Atención médica infantil"));

        Profesional medico = new Profesional(null, "Maria", "Gómez", "87654321", "M-54321", "maria@hospital.com", "261999888");
        medico.getEspecialidades().add(pediatria);
        medico = profesionalRepository.save(medico);

        Sede hospitalCentral = sedeRepository.save(new Sede("Hospital Central", "Alem 450", "2614490000", "Mendoza"));

        Turno turno = new Turno(null, LocalDateTime.of(2026, 10, 1, 10, 30), EstadoTurno.RESERVADO, paciente, medico, pediatria, hospitalCentral, "Consulta médica general");
        Turno guardado = turnoRepository.save(turno);

        assertThat(guardado.getId()).isNotNull();
        assertThat(guardado.getFechaCreacion()).isNotNull();

        List<Turno> turnosPaciente = turnoRepository.findByPacienteId(paciente.getId());
        assertThat(turnosPaciente).hasSize(1);
        assertThat(turnosPaciente.get(0).getProfesional().getApellido()).isEqualTo("Gómez");
        assertThat(turnosPaciente.get(0).getPaciente().getObraSocial()).isNotNull();
    }
}
