package com.example.acceso.service.Interfaces;

import com.example.acceso.model.SerieComprobante;

import java.util.List;
import java.util.Optional;

public interface SerieComprobanteService {

    List<SerieComprobante> listarSerieComprobante();

    Optional<SerieComprobante> obtenerSerieComprobantePorId(Long id);

    Optional<SerieComprobante> eliminarLogicoSerieComprobante(Long id);

    Optional<SerieComprobante> cambiarEstadoSerieComprobante(Long id);

}
