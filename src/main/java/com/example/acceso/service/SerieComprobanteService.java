package com.example.acceso.service;

import com.example.acceso.model.SerieComprobante;
import com.example.acceso.repository.SerieComprobanteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SerieComprobanteService {

    private final SerieComprobanteRepository serieComprobanteRepository;


    public SerieComprobanteService(SerieComprobanteRepository serieComprobanteRepository) {
        this.serieComprobanteRepository = serieComprobanteRepository;
    }

    @Transactional(readOnly = true)
    public List<SerieComprobante> listarSerieComprobante() {
        return serieComprobanteRepository.findAllByEstadoNot(2);
    }

    @Transactional(readOnly = true)
    public Optional<SerieComprobante> obtenerSerieComprobantePorId(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }

        return serieComprobanteRepository.findById(id);
    }

    @Transactional
    public List<SerieComprobante> listarSerieComprobanteActivas() {
        return serieComprobanteRepository.findAllByEstadoNot(2);
    }

    @Transactional
    public Optional<SerieComprobante> eliminarSerieComprobante(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }

        return obtenerSerieComprobantePorId(id).map(serieComprobante -> {
            serieComprobante.setEstado(2); // Eliminar
            return serieComprobanteRepository.save(serieComprobante);
        });
    }

    @Transactional
    public Optional<SerieComprobante> cambiarEstadoSerieComprobante(Long id, Integer estado) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }

        return obtenerSerieComprobantePorId(id).map(serieComprobante -> {
            if (serieComprobante.getEstado() == 1) {
                serieComprobante.setEstado(0); // Desactivar
            } else if (serieComprobante.getEstado() == 0) {
                serieComprobante.setEstado(1); // Activar
            }
            return serieComprobanteRepository.save(serieComprobante);
        });
    }

}
