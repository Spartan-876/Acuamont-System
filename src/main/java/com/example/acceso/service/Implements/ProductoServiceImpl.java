package com.example.acceso.service.Implements;

import com.example.acceso.model.Producto;
import com.example.acceso.repository.ProductoRepository;
import com.example.acceso.service.Interfaces.ProductoService;
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

/**
 * Servicio para gestionar la lógica de negocio de los productos.
 *
 * Proporciona métodos para operaciones CRUD (Crear, Leer, Actualizar, Eliminar)
 * sobre las entidades de Producto, incluyendo la gestión de imágenes asociadas.
 */
@Service
public class ProductoServiceImpl implements ProductoService {

    /**
     * Directorio donde se subirán las imágenes de los productos.
     * Inyectado desde el archivo de propiedades (application.properties).
     */
    @Value("${file.upload-dir}")
    private String uploadDir;

    private final ProductoRepository productoRepository;

    /**
     * Constructor para la inyección de dependencias del repositorio de productos.
     *
     * @param productoRepository El repositorio para las operaciones de base de
     *                           datos de Producto.
     */
    public ProductoServiceImpl(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    /**
     * Obtiene una lista de todos los productos que no están eliminados lógicamente.
     *
     * @return Una lista de objetos {@link Producto}.
     */
    @Transactional(readOnly = true)
    public List<Producto> listarProductos() {
        return productoRepository.findAllByEstadoNot(2);
    }

    /**
     * Guarda o actualiza un producto en la base de datos, y maneja la subida de su
     * imagen.
     * <p>
     * Si el producto tiene un ID, se actualiza. Si no, se crea uno nuevo.
     * Si se proporciona un nuevo archivo de imagen, la imagen anterior (si existe)
     * se elimina
     * y la nueva se guarda.
     *
     * @param producto El objeto {@link Producto} a guardar.
     * @param fotoFile El archivo de imagen ({@link MultipartFile}) del producto,
     *                 puede ser nulo.
     * @return El producto guardado con su ID y la ruta de la imagen actualizados.
     * @throws IllegalArgumentException Si faltan campos obligatorios, si ya existe
     *                                  un producto con el mismo nombre,
     *                                  o si ocurre un error al guardar la imagen o
     *                                  los datos.
     */
    @Transactional
    public Producto guardarProducto(Producto producto, MultipartFile fotoFile) {
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

        } catch (Exception e) {
            throw new IllegalArgumentException("Error al guardar el producto: " + e.getMessage(), e);
        }
    }

    /**
     * Cuenta el número total de productos que no están eliminados.
     *
     * @return El número de productos activos e inactivos.
     */
    @Transactional(readOnly = true)
    public long contarProductos() {
        return productoRepository.countByEstadoNot(2);
    }

    /**
     * Busca un producto por su ID.
     *
     * @param id El ID del producto a buscar.
     * @return Un {@link Optional} que contiene el producto si se encuentra, o un
     *         Optional vacío si no.
     */
    @Transactional(readOnly = true)
    public Optional<Producto> obtenerProductoPorId(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        return productoRepository.findById(id);
    }

    /**
     * Busca un producto por su nombre.
     *
     * @param nombre El nombre del producto a buscar.
     * @return Un {@link Optional} que contiene el producto si se encuentra, o un
     *         Optional vacío si no.
     */
    @Transactional(readOnly = true)
    public Optional<Producto> obtenerProductoPorNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return Optional.empty();
        }
        return productoRepository.findByNombre(nombre.trim().toLowerCase());
    }

    /**
     * Realiza el borrado lógico de un producto, cambiando su estado a 2.
     * También elimina la imagen asociada del sistema de archivos.
     *
     * @param id El ID del producto a eliminar.
     * @throws IllegalArgumentException si el ID es inválido o el producto no se
     *                                  encuentra.
     */
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

    /**
     * Cambia el estado de un producto entre activo (1) e inactivo (0).
     * <p>
     * Si el producto está eliminado (estado 2), no se realiza ningún cambio.
     *
     * @param id El ID del producto cuyo estado se va a cambiar.
     * @return Un {@link Optional} con el producto actualizado si se encontró, o un
     *         Optional vacío si no.
     */
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

    /**
     * Guarda un archivo de imagen en el directorio de subidas.
     * Genera un nombre de archivo único para evitar colisiones.
     *
     * @param fotoFile El archivo de imagen a guardar.
     * @return El nombre único del archivo guardado.
     * @throws IOException Si ocurre un error durante la escritura del archivo.
     */
    private String guardarImagen(MultipartFile fotoFile) throws IOException {
        // Genera un nombre de archivo único para evitar colisiones
        String nombreUnico = UUID.randomUUID().toString() + "_" + fotoFile.getOriginalFilename();
        Path rutaCompleta = Paths.get(uploadDir + nombreUnico);

        Files.createDirectories(((java.nio.file.Path) rutaCompleta).getParent());

        Files.write(rutaCompleta, fotoFile.getBytes());
        return nombreUnico;
    }

    /**
     * Elimina un archivo de imagen del directorio de subidas.
     *
     * @param nombreImagen El nombre del archivo a eliminar.
     * @throws IllegalArgumentException Si ocurre un error al eliminar el archivo.
     */
    private void eliminarImagen(String nombreImagen) {
        if (nombreImagen == null || nombreImagen.isEmpty()) {
            return;
        }
        try {
            Path rutaImagen = Paths.get(uploadDir + nombreImagen);
            Files.deleteIfExists(rutaImagen);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error al eliminar la imagen: " + nombreImagen + " - " + e.getMessage(),
                    e);
        }
    }

}
