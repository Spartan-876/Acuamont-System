package com.example.acceso.service;

import com.example.acceso.model.Cliente;
import com.example.acceso.repository.ClienteRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar la lógica de negocio de los clientes.
 *
 * Proporciona métodos para operaciones CRUD (Crear, Leer, Actualizar, Eliminar)
 * sobre las entidades de Cliente, manejando la lógica de negocio como
 * validaciones y transacciones.
 */
@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    /**
     * Constructor para la inyección de dependencias del repositorio de clientes.
     *
     * @param clienteRepository El repositorio para las operaciones de base de datos
     *                          de Cliente.
     */
    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    /**
     * Obtiene una lista de todos los clientes que no están eliminados lógicamente.
     *
     * @return Una lista de objetos {@link Cliente}.
     */
    @Transactional(readOnly = true)
    public List<Cliente> listarClientes() {
        return clienteRepository.findAllByEstadoNot(2);
    }

    /**
     * Guarda o actualiza un cliente en la base de datos.
     * <p>
     * Si el cliente tiene un ID, se actualiza. Si no, se crea uno nuevo.
     * Realiza validaciones para asegurar que los campos obligatorios no estén
     * vacíos
     * y que el número de documento sea único.
     *
     * @param cliente El objeto {@link Cliente} a guardar.
     * @return El cliente guardado con su ID asignado o actualizado.
     * @throws IllegalArgumentException Si falta un campo obligatorio, si el
     *                                  documento ya existe para otro cliente,
     *                                  o si ocurre otro error de integridad de
     *                                  datos.
     */
    @Transactional
    public Cliente guardarCliente(Cliente cliente) {
        try {
            if (cliente.getNombre() == null || cliente.getNombre().trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre es obligatorio");
            }

            if (cliente.getDocumento() == null || cliente.getDocumento().trim().isEmpty()) {
                throw new IllegalArgumentException("El documento es obligatorio");
            }

            Optional<Cliente> existente = clienteRepository.findByDocumento(cliente.getDocumento());
            if (existente.isPresent() && !existente.get().getId().equals(cliente.getId())) {
                throw new IllegalArgumentException("Ya existe un cliente con el mismo documento");
            }

            return clienteRepository.save(cliente);
        } catch (DataIntegrityViolationException e) {
            String message = e.getMessage().toLowerCase();
            if (message.contains("documento")) {
                throw new IllegalArgumentException("Ya existe un cliente con el mismo documento");
            } else {
                throw new IllegalArgumentException("Error de integridad de datos");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Error al guardar el cliente: " + e.getMessage(), e);
        }
    }

    /**
     * Cuenta el número total de clientes que no están eliminados.
     *
     * @return El número de clientes activos e inactivos.
     */
    @Transactional(readOnly = true)
    public long contarClientes() {
        return clienteRepository.countByEstadoNot(2);
    }

    /**
     * Busca un cliente por su ID.
     *
     * @param id El ID del cliente a buscar.
     * @return Un {@link Optional} que contiene el cliente si se encuentra, o un
     *         Optional vacío si no.
     */
    @Transactional(readOnly = true)
    public Optional<Cliente> obtenerClientePorId(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        return clienteRepository.findById(id);
    }

    /**
     * Busca un cliente por su número de documento.
     *
     * @param documento El número de documento del cliente a buscar.
     * @return Un {@link Optional} que contiene el cliente si se encuentra, o un
     *         Optional vacío si no.
     */
    @Transactional(readOnly = true)
    public Optional<Cliente> obtenerClientePorDocumento(String documento) {
        return clienteRepository.findByDocumento(documento);
    }

    /**
     * Realiza el borrado lógico de un cliente, cambiando su estado a 2.
     *
     * @param id El ID del cliente a eliminar.
     * @throws IllegalArgumentException si el ID es inválido o el cliente no se
     *                                  encuentra.
     */
    @Transactional
    public void eliminarCliente(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID de cliente inválido");
        }
        Cliente cliente = obtenerClientePorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        cliente.setEstado(2);
        clienteRepository.save(cliente);
    }

    /**
     * Cambia el estado de un cliente entre activo (1) e inactivo (0).
     * <p>
     * Si el cliente está eliminado (estado 2), no se realiza ningún cambio.
     *
     * @param id El ID del cliente cuyo estado se va a cambiar.
     * @return Un {@link Optional} con el cliente actualizado si se encontró, o un
     *         Optional vacío si no.
     */
    @Transactional
    public Optional<Cliente> cambiarEstadoCliente(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID de cliente inválido");
        }

        return obtenerClientePorId(id).map(cliente -> {
            if (cliente.getEstado() == 1) {
                cliente.setEstado(0);
            } else if (cliente.getEstado() == 0) {
                cliente.setEstado(1);
            }
            // No se hace nada si el estado es 2 (eliminado)
            return clienteRepository.save(cliente);
        });
    }

}
