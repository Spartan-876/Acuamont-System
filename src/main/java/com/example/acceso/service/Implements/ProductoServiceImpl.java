package com.example.acceso.service.Implements;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.acceso.model.Producto;
import com.example.acceso.repository.ProductoRepository;
import com.example.acceso.service.Interfaces.ProductoService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ProductoServiceImpl implements ProductoService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final ProductoRepository productoRepository;

    public ProductoServiceImpl(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<Producto> listarProductos() {
        return productoRepository.findAllByEstadoNot(2);
    }

    @Transactional
    public Producto guardarProducto(Producto producto, List<MultipartFile> fotos) {
        try {
            // Validations
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

            producto.setNombre(producto.getNombre().trim());
            producto.setDescripcion(producto.getDescripcion().trim());

            // If it's a new product, save it first to get an ID
            boolean isNewProduct = producto.getId() == null;
            if (isNewProduct) {
                producto.setImagen("[]"); // Initialize with an empty JSON array
                producto = productoRepository.save(producto);
            }

            // Fetch the entity to ensure we're working with a managed instance
            Producto finalProducto = producto;
            Producto productoParaActualizar = productoRepository.findById(producto.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Error al guardar, no se encontró el producto con ID: " + finalProducto.getId()));

            // Handle file uploads
            if (fotos != null && !fotos.isEmpty() && fotos.stream().anyMatch(f -> !f.isEmpty())) {
                Path carpetaProducto = Paths.get(uploadDir, productoParaActualizar.getId().toString());

                ObjectMapper objectMapper = new ObjectMapper();
                List<String> nombresImagenes;
                String jsonImagenesActual = productoParaActualizar.getImagen();

                // Deserialize existing images
                if (jsonImagenesActual != null && !jsonImagenesActual.isEmpty() && !"[]".equals(jsonImagenesActual)) {
                    nombresImagenes = objectMapper.readValue(jsonImagenesActual, new TypeReference<List<String>>() {});
                } else {
                    nombresImagenes = new ArrayList<>();
                }

                // Save new images and add them to the list
                for (MultipartFile foto : fotos) {
                    if (foto != null && !foto.isEmpty()) {
                        String nombreImagen = guardarImagen(foto, carpetaProducto);
                        nombresImagenes.add(nombreImagen);
                    }
                }

                // Serialize the updated list back to JSON
                String jsonImagenesNuevo = objectMapper.writeValueAsString(nombresImagenes);
                productoParaActualizar.setImagen(jsonImagenesNuevo);
            }
            
            // Update other product fields from the input object
            productoParaActualizar.setNombre(producto.getNombre());
            productoParaActualizar.setDescripcion(producto.getDescripcion());
            productoParaActualizar.setPrecioCompra(producto.getPrecioCompra());
            productoParaActualizar.setPrecioVenta(producto.getPrecioVenta());
            productoParaActualizar.setStock(producto.getStock());
            productoParaActualizar.setStockSeguridad(producto.getStockSeguridad());
            productoParaActualizar.setEstado(producto.getEstado());

            return productoRepository.save(productoParaActualizar);

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

        // Delete the entire product folder
        eliminarCarpetaProducto(producto);

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

    /**
     * Saves an image file into the product's specific directory.
     *
     * @param fotoFile The image file to save.
     * @param carpetaProducto The path to the product's directory.
     * @return The unique name of the saved file.
     * @throws IOException If an error occurs during file writing.
     */
    private String guardarImagen(MultipartFile fotoFile, Path carpetaProducto) throws IOException {
        Files.createDirectories(carpetaProducto);
        String nombreUnico = UUID.randomUUID().toString() + "_" + fotoFile.getOriginalFilename();
        Path rutaCompleta = carpetaProducto.resolve(nombreUnico);
        Files.write(rutaCompleta, fotoFile.getBytes());
        return nombreUnico;
    }

    /**
     * Deletes the entire folder associated with a product, including all its images.
     *
     * @param producto The product whose folder is to be deleted.
     */
    private void eliminarCarpetaProducto(Producto producto) {
        if (producto == null || producto.getId() == null) {
            return;
        }
        try {
            Path carpetaProducto = Paths.get(uploadDir, producto.getId().toString());

            if (Files.exists(carpetaProducto) && Files.isDirectory(carpetaProducto)) {
                Files.walk(carpetaProducto)
                     .sorted(Comparator.reverseOrder())
                     .map(Path::toFile)
                     .forEach(File::delete);
            }
        } catch (IOException e) {
            System.err.println("Error al eliminar la carpeta del producto " + producto.getId() + ": " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void eliminarImagen(Long productoId, String nombreImagen) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + productoId));

        String jsonImagenes = producto.getImagen();
        if (jsonImagenes == null || jsonImagenes.isEmpty()) {
            throw new IllegalArgumentException("El producto no tiene imágenes asociadas.");
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<String> nombresImagenes = objectMapper.readValue(jsonImagenes, new TypeReference<List<String>>() {});

            if (!nombresImagenes.contains(nombreImagen)) {
                throw new IllegalArgumentException("La imagen no pertenece al producto.");
            }
            
            // Construct path and delete the physical file
            Path carpetaProducto = Paths.get(uploadDir, producto.getId().toString());
            eliminarArchivoImagen(carpetaProducto.resolve(nombreImagen));

            // Remove from list and update the product
            nombresImagenes.remove(nombreImagen);
            String nuevoJsonImagenes = objectMapper.writeValueAsString(nombresImagenes);
            producto.setImagen(nuevoJsonImagenes);
            productoRepository.save(producto);

        } catch (IOException e) {
            throw new RuntimeException("Error al procesar las imágenes del producto.", e);
        }
    }

    /**
     * Deletes a single image file from the filesystem.
     *
     * @param rutaImagen The full path to the image file to be deleted.
     */
    private void eliminarArchivoImagen(Path rutaImagen) {
        try {
            Files.deleteIfExists(rutaImagen);
        } catch (IOException e) {
            System.err.println("Error al eliminar la imagen: " + rutaImagen.toString() + " - " + e.getMessage());
        }
    }

    @Override
    public List<Object[]> findTop5ProductosMasVendidos() {
        return productoRepository.findTop5ProductosMasVendidos();
    }
}
