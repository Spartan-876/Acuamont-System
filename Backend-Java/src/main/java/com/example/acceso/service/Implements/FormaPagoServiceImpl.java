package com.example.acceso.service.Implements;

import com.example.acceso.model.FormaPago;
import com.example.acceso.repository.FormaPagoRepository;
import com.example.acceso.service.Interfaces.FormaPagoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar la lógica de negocio de las formas de pago.
 *
 * Proporciona métodos para listar y obtener las formas de pago disponibles
 * en el sistema, que se utilizarán principalmente en el proceso de venta.
 */
@Service
public class FormaPagoServiceImpl implements FormaPagoService {

    private final FormaPagoRepository formaPagoRepository;

    /**
     * Constructor para la inyección de dependencias del repositorio de formas de pago.
     *
     * @param formaPagoRepository El repositorio para las operaciones de base de datos de {@link FormaPago}.
     */
    public FormaPagoServiceImpl(FormaPagoRepository formaPagoRepository) {
        this.formaPagoRepository = formaPagoRepository;
    }

    /**
     * Obtiene una lista de todas las formas de pago que no están eliminadas lógicamente.
     *
     * @return Una lista de objetos {@link FormaPago}.
     */
    @Transactional(readOnly = true)
    public List<FormaPago> listarFormasPago() {
        return formaPagoRepository.findAllByEstadoNot(2);
    }

    /**
     * Busca una forma de pago por su ID.
     *
     * @param id El ID de la forma de pago a buscar.
     * @return Un {@link Optional} que contiene la forma de pago si se encuentra, o un Optional vacío si no.
     */
    @Transactional(readOnly = true)
    public Optional<FormaPago> obtenerFormaPagoPorId(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }

        return formaPagoRepository.findById(id);
    }

}
