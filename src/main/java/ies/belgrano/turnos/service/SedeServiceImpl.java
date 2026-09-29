package ies.belgrano.turnos.service;

import ies.belgrano.turnos.dto.SedeDTO;
import ies.belgrano.turnos.repository.SedeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SedeServiceImpl implements SedeService {

    private final SedeRepository sedeRepository;

    public SedeServiceImpl(SedeRepository sedeRepository) {
        this.sedeRepository = sedeRepository;
    }

    @Override
    public List<SedeDTO> listarSedes() {
        return sedeRepository.findAll().stream()
                .map(sede -> new SedeDTO(sede.getId(), sede.getNombre(), sede.getDireccion()))
                .collect(Collectors.toList());
    }
}