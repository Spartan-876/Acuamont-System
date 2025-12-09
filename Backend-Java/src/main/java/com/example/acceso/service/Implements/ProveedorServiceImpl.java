package com.example.acceso.service.Implements;

import com.example.acceso.model.Proveedor;
import com.example.acceso.repository.ProveedorRepository;
import com.example.acceso.service.Interfaces.ProveedorService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
public class ProveedorServiceImpl implements ProveedorService {

    private final ProveedorRepository proveedorRepository;
    
    public ProveedorServiceImpl(ProveedorRepository proveedorRepository) {
        this.proveedorRepository = proveedorRepository;
    }

    @Transactional(readOnly = true)
    public List<Proveedor> listarProveedores() {
        return proveedorRepository.findAllByEstadoNot(2);
    }
    
    @Transactional
    public Proveedor guardarProveedor(Proveedor proveedor) {
        try {
            if (proveedor.getNombre() == null || proveedor.getNombre().trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre es obligatorio");
            }

            if (proveedor.getDocumento() == null || proveedor.getDocumento().trim().isEmpty()) {
                throw new IllegalArgumentException("El documento es obligatorio");
            }

            Optional<Proveedor> existente = proveedorRepository.findByDocumento(proveedor.getDocumento());
            if (existente.isPresent() && !existente.get().getId().equals(proveedor.getId())) {
                throw new IllegalArgumentException("Ya existe un proveedor con el mismo documento");
            }

            return proveedorRepository.save(proveedor);
        } catch (DataIntegrityViolationException e) {
            String message = e.getMessage().toLowerCase();
            if (message.contains("documento")) {
                throw new IllegalArgumentException("Ya existe un proveedor con el mismo documento");
            } else {
                throw new IllegalArgumentException("Error de integridad de datos");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Error al guardar el proveedor: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public long contarProveedores() {
        return proveedorRepository.countByEstadoNot(2);
    }

    @Transactional(readOnly = true)
    public Optional<Proveedor> obtenerProveedorPorId(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        return proveedorRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Proveedor> obtenerProveedorPorDocumento(String documento) {
        return proveedorRepository.findByDocumento(documento);
    }

    @Transactional
    public void eliminarProveedor(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID de proveedor inválido");
        }
        Proveedor proveedor = obtenerProveedorPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));
        proveedor.setEstado(2);
        proveedorRepository.save(proveedor);
    }

    @Transactional
    public Optional<Proveedor> cambiarEstadoProveedor(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID de proveedor inválido");
        }

        return obtenerProveedorPorId(id).map(proveedor -> {
            if (proveedor.getEstado() == 1) {
                proveedor.setEstado(0);
            } else if (proveedor.getEstado() == 0) {
                proveedor.setEstado(1);
            }
            // No se hace nada si el estado es 2 (eliminado)
            return proveedorRepository.save(proveedor);
        });
    }

}
