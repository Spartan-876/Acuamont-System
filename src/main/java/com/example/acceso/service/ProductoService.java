package com.example.acceso.service;

import com.example.acceso.model.Producto;
import com.example.acceso.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductoService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<Producto> listarProductos() {
        return productoRepository.findAllByEstadoNot(2);
    }

    @Transactional
    public Producto guardarProducto(Producto producto,  MultipartFile fotoFile) {
        try {
            if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre es obligatorio");
            }

            if (producto.getDescripcion() == null || producto.getDescripcion().trim().isEmpty()) {
                throw new IllegalArgumentException("La descripción es obligatoria");
            }

            if (producto.getPrecioCompra() == null) {
                throw new IllegalArgumentException("El precio de compra es obligatorio");
            }

            if (producto.getPrecioVenta() == null) {
                throw new IllegalArgumentException("El precio de venta es obligatorio");
            }

            if (producto.getStock() == null) {
                throw new IllegalArgumentException("El stock es obligatorio");
            }

            if (producto.getStockSeguridad() == null) {
                throw new IllegalArgumentException("El stock de seguridad es obligatorio");
            }

            if (fotoFile != null && !fotoFile.isEmpty()) {
                if (producto.getId() != null && producto.getImagen() != null) {
                    eliminarImagen(producto.getImagen());
                }
                String nombreImagen = guardarImagen(fotoFile);
                producto.setImagen(nombreImagen);
            }

            producto.setNombre(producto.getNombre().trim());
            producto.setDescripcion(producto.getDescripcion().trim());

            return productoRepository.save(producto);
        } catch (DataIntegrityViolationException e) {
            String message = e.getMessage().toLowerCase();
            if (message.contains("nombre")) {
                throw new IllegalArgumentException("Ya existe un producto con el mismo nombre");
            } else if (message.contains("descripcion")) {
                throw new IllegalArgumentException("Ya existe un producto con la misma descripción");
            } else {
                throw new IllegalArgumentException("Error de integridad de datos");
            }

        }catch (Exception e) {
            throw new IllegalArgumentException("Error al guardar el producto: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public long contarProductos() {
        return productoRepository.countByEstadoNot(2);
    }

    @Transactional(readOnly = true)
    public Optional<Producto> obtenerProductoPorId(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        return productoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Producto> obtenerProductoPorNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return Optional.empty();
        }
        return productoRepository.findByNombre(nombre.trim().toLowerCase());
    }

    @Transactional
    public void eliminarProducto(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID de producto inválido");
        }
        Producto producto = obtenerProductoPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        eliminarImagen(producto.getImagen());
        producto.setImagen(null);
        producto.setEstado(2);
        productoRepository.save(producto);
    }

    @Transactional
    public Optional<Producto> cambiarEstadoProducto(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }

        return obtenerProductoPorId(id).map(producto -> {
            if (producto.getEstado() == 1) {
                producto.setEstado(0);
            } else if (producto.getEstado() == 0) {
                producto.setEstado(1);
            }
            return productoRepository.save(producto);
        });
    }

    //Control de imagenes

    private String guardarImagen(MultipartFile fotoFile) throws IOException {
        // Genera un nombre de archivo único para evitar colisiones
        String nombreUnico = UUID.randomUUID().toString() + "_" + fotoFile.getOriginalFilename();
        Path rutaCompleta = Paths.get(uploadDir + nombreUnico);

        Files.createDirectories(((java.nio.file.Path) rutaCompleta).getParent());

        Files.write(rutaCompleta, fotoFile.getBytes());
        return nombreUnico;
    }

    private void eliminarImagen(String nombreImagen) {
        if (nombreImagen == null || nombreImagen.isEmpty()) {
            return;
        }
        try {
            Path rutaImagen = Paths.get(uploadDir + nombreImagen);
            Files.deleteIfExists(rutaImagen);
        }catch (IOException e) {
            throw new IllegalArgumentException("Error al eliminar la imagen: "+ nombreImagen + " - " + e.getMessage(), e);
        }
    }

}
