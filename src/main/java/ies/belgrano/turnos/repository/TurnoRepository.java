package ies.belgrano.turnos.repository;

import ies.belgrano.turnos.model.EstadoTurno;
import ies.belgrano.turnos.model.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TurnoRepository extends JpaRepository<Turno, Long> {

    List<Turno> findByPacienteId(Long pacienteId);

    List<Turno> findByProfesionalId(Long profesionalId);

    List<Turno> findByEstado(EstadoTurno estado);

    List<Turno> findByEstadoOrderByFechaHoraInicioAsc(EstadoTurno estado);

    List<Turno> findBySedeIdAndEspecialidadId(Long sedeId, Long especialidadId);

    List<Turno> findByProfesionalIdAndFechaHoraBetween(Long profesionalId, LocalDateTime inicio, LocalDateTime fin);

    List<Turno> findBySedeIdAndEspecialidadIdAndEstado(Long sedeId, Long especialidadId, EstadoTurno estado);

    boolean existsByProfesionalIdAndFechaHoraAndEstadoNot(Long profesionalId, LocalDateTime fechaHora, EstadoTurno estado);

    Optional<Turno> findFirstByProfesionalIdAndFechaHoraInicio(Long profesionalId, LocalDateTime fechaHoraInicio);

    Optional<Turno> findFirstByProfesionalIdAndFechaHora(Long profesionalId, LocalDateTime fechaHora);

    @Query("SELECT t FROM Turno t WHERE t.profesional.id = :profesionalId " +
           "AND t.estado != :estadoCancelado " +
           "AND t.fechaHoraInicio < :fin " +
           "AND t.fechaHoraFin > :inicio")
    List<Turno> findSolapamientos(@Param("profesionalId") Long profesionalId,
                                  @Param("inicio") LocalDateTime inicio,
                                  @Param("fin") LocalDateTime fin,
                                  @Param("estadoCancelado") EstadoTurno estadoCancelado);

    @Query("SELECT t FROM Turno t WHERE t.estado = :estado " +
           "AND (:especialidadId IS NULL OR t.especialidad.id = :especialidadId) " +
           "AND (:profesionalId IS NULL OR t.profesional.id = :profesionalId) " +
           "AND (:sedeId IS NULL OR t.sede.id = :sedeId) " +
           "AND COALESCE(t.fechaHoraInicio, t.fechaHora) >= :ahora " +
           "AND (:inicioDia IS NULL OR COALESCE(t.fechaHoraInicio, t.fechaHora) >= :inicioDia) " +
           "AND (:finDia IS NULL OR COALESCE(t.fechaHoraInicio, t.fechaHora) <= :finDia) " +
           "ORDER BY COALESCE(t.fechaHoraInicio, t.fechaHora) ASC")
    List<Turno> findDisponibles(@Param("estado") EstadoTurno estado,
                                @Param("especialidadId") Long especialidadId,
                                @Param("profesionalId") Long profesionalId,
                                @Param("sedeId") Long sedeId,
                                @Param("ahora") LocalDateTime ahora,
                                @Param("inicioDia") LocalDateTime inicioDia,
                                @Param("finDia") LocalDateTime finDia);

    @Query("SELECT t FROM Turno t WHERE (:estado IS NULL OR t.estado = :estado) " +
           "AND (:especialidadId IS NULL OR t.especialidad.id = :especialidadId) " +
           "AND (:profesionalId IS NULL OR t.profesional.id = :profesionalId) " +
           "AND (:sedeId IS NULL OR t.sede.id = :sedeId) " +
           "AND (:inicioDia IS NULL OR t.fechaHoraInicio >= :inicioDia) " +
           "AND (:finDia IS NULL OR t.fechaHoraInicio <= :finDia) " +
           "ORDER BY t.fechaHoraInicio ASC")
    List<Turno> findAgenda(@Param("estado") EstadoTurno estado,
                           @Param("especialidadId") Long especialidadId,
                           @Param("profesionalId") Long profesionalId,
                           @Param("sedeId") Long sedeId,
                           @Param("inicioDia") LocalDateTime inicioDia,
                           @Param("finDia") LocalDateTime finDia);
}
