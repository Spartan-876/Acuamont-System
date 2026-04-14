package com.example.acceso.service.Implements;

import com.example.acceso.model.SerieComprobante;
import com.example.acceso.repository.SerieComprobanteRepository;
import com.example.acceso.service.Interfaces.SerieComprobanteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar la lógica de negocio de las series de comprobantes.
 *
 * Proporciona métodos para listar, obtener, eliminar y cambiar el estado de las
 * series de comprobantes utilizadas en las ventas.
 */
@Service
public class SerieComprobanteServiceImpl implements SerieComprobanteService {

    private final SerieComprobanteRepository serieComprobanteRepository;

    /**
     * Constructor para la inyección de dependencias del repositorio de series de
     * comprobantes.
     *
     * @param serieComprobanteRepository El repositorio para las operaciones de base
     *                                   de datos de {@link SerieComprobante}.
     */
    public SerieComprobanteServiceImpl(SerieComprobanteRepository serieComprobanteRepository) {
        this.serieComprobanteRepository = serieComprobanteRepository;
    }

    /**
     * Obtiene una lista de todas las series de comprobante que no están eliminadas
     * lógicamente.
     *
     * @return Una lista de objetos {@link SerieComprobante}.
     */
    @Transactional(readOnly = true)
    public List<SerieComprobante> listarSerieComprobante() {
        return serieComprobanteRepository.findAllByEstadoNot(2);
    }

    /**
     * Busca una serie de comprobante por su ID.
     *
     * @param id El ID de la serie de comprobante a buscar.
     * @return Un {@link Optional} que contiene la serie si se encuentra, o un
     *         Optional vacío si no.
     */
    @Transactional(readOnly = true)
    public Optional<SerieComprobante> obtenerSerieComprobantePorId(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        return serieComprobanteRepository.findById(id);
    }

    /**
     * Realiza el borrado lógico de una serie de comprobante, cambiando su estado a
     * 2.
     *
     * @param id El ID de la serie de comprobante a eliminar.
     * @return Un {@link Optional} con la serie actualizada si se encontró y
     *         eliminó, o un Optional vacío si no.
     */
    @Transactional
    public Optional<SerieComprobante> eliminarLogicoSerieComprobante(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }

        return obtenerSerieComprobantePorId(id).map(serieComprobante -> {
            serieComprobante.setEstado(2); // Estado 2 = Eliminado
            return serieComprobanteRepository.save(serieComprobante);
        });
    }

    /**
     * Cambia el estado de una serie de comprobante entre activo (1) e inactivo (0).
     *
     * @param id El ID de la serie de comprobante cuyo estado se va a cambiar.
     * @return Un {@link Optional} con la serie actualizada si se encontró, o un
     *         Optional vacío si no.
     */
    @Transactional
    public Optional<SerieComprobante> cambiarEstadoSerieComprobante(Long id) {
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
