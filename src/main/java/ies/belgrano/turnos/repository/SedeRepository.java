package ies.belgrano.turnos.repository;

import ies.belgrano.turnos.model.Sede;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SedeRepository extends JpaRepository<Sede, Long> {
    Optional<Sede> findByNombreIgnoreCase(String nombre);
    List<Sede> findByCiudadIgnoreCase(String ciudad);
}
