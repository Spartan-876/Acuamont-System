package com.example.acceso.service;

import com.example.acceso.model.Cliente;
import com.example.acceso.repository.ClienteRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

        private final ClienteRepository clienteRepository;

        public ClienteService(ClienteRepository clienteRepository) {
            this.clienteRepository = clienteRepository;
        }

        @Transactional
        public List<Cliente> listarClientes() {
            return clienteRepository.findAllByEstadoNot(2);
        }

        @Transactional
        public Cliente guardarCliente(Cliente cliente) {
            try{
                if (cliente.getNombre()==null || cliente.getNombre().trim().isEmpty() ) {
                    throw new IllegalArgumentException("El nombre es obligatorio");
                }

                if (cliente.getDocumento()==null || cliente.getDocumento().trim().isEmpty() ) {
                    throw new IllegalArgumentException("El documento es obligatorio");
                }

                if (cliente.getTelefono()==null || cliente.getTelefono().trim().isEmpty() ) {
                    throw new IllegalArgumentException("El teléfono es obligatorio");
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

        @Transactional(readOnly = true)
        public long contarClientes() {
           return clienteRepository.countByEstado(2);
        }

        @Transactional(readOnly = true)
        public Optional<Cliente> obtenerClientePorId(Long id) {
            if (id == null || id <= 0) {
                return Optional.empty();
            }
            return clienteRepository.findById(id);
        }

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

        @Transactional
        public Optional<Cliente> cambiarEstadoCliente(Long id) {
            if (id == null || id <= 0) {
                throw new IllegalArgumentException("ID de cliente inválido");
            }

            return obtenerClientePorId(id).map(cliente -> {
                if (cliente.getEstado() == 1) {
                    cliente.setEstado(0);
                } else if (cliente.getEstado() == 0) {
                    cliente.setEstado(1);
                }
                return clienteRepository.save(cliente);
            });
        }

}
