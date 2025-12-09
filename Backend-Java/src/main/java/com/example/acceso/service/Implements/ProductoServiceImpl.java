package com.example.acceso.service.Implements;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.cloudinary.utils.ObjectUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
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

    private final ProductoRepository productoRepository;
    private final CloudinaryService cloudinaryService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ProductoServiceImpl(ProductoRepository productoRepository, CloudinaryService cloudinaryService) {
        this.productoRepository = productoRepository;
        this.cloudinaryService = cloudinaryService;
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
                List<String> listaImagenes;
                String jsonImagenesActual = productoParaActualizar.getImagen();
                if (jsonImagenesActual != null && !jsonImagenesActual.isEmpty() && !"[]".equals(jsonImagenesActual)) {
                    try {
                        listaImagenes = objectMapper.readValue(jsonImagenesActual, new TypeReference<List<String>>() {});
                    } catch (JsonProcessingException e) {
                        listaImagenes = new ArrayList<>();
                    }
                } else {
                    listaImagenes = new ArrayList<>();
                }
                String nombreCarpetaNube = "productos_acuamont/" + productoParaActualizar.getId();
                for (MultipartFile foto : fotos) {
                    if (foto != null && !foto.isEmpty()) {
                        String urlImagen = cloudinaryService.subirImagen(foto, nombreCarpetaNube);
                        listaImagenes.add(urlImagen);
                    }
                }
                String jsonImagenesNuevo = objectMapper.writeValueAsString(listaImagenes);
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
        if (id == null || id <= 0) throw new IllegalArgumentException("ID inválido");

        Producto producto = obtenerProductoPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        eliminarTodasLasImagenesDeCloudinary(producto);

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

    @Override
    @Transactional
    public void eliminarImagen(Long productoId, String urlImagenAEliminar) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        String jsonImagenes = producto.getImagen();
        if (jsonImagenes == null || jsonImagenes.isEmpty()) {
            throw new IllegalArgumentException("El producto no tiene imágenes.");
        }

        try {
            List<String> listaUrls = objectMapper.readValue(jsonImagenes, new TypeReference<List<String>>() {});

            if (!listaUrls.contains(urlImagenAEliminar)) {
                throw new IllegalArgumentException("La imagen no pertenece al producto.");
            }

            String publicId = obtenerPublicId(urlImagenAEliminar);
            if (publicId != null) {
                cloudinaryService.eliminarImagen(publicId, ObjectUtils.emptyMap());
            }

            listaUrls.remove(urlImagenAEliminar);
            String nuevoJson = objectMapper.writeValueAsString(listaUrls);
            producto.setImagen(nuevoJson);
            productoRepository.save(producto);

        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar imagen: " + e.getMessage(), e);
        }
    }

    private String obtenerPublicId(String url) {
        try {
            int inicio = url.indexOf("productos_acuamont/");
            if (inicio == -1) return null;

            String rutaConExt = url.substring(inicio);

            int punto = rutaConExt.lastIndexOf(".");
            if (punto != -1) {
                return rutaConExt.substring(0, punto);
            }
            return rutaConExt;
        } catch (Exception e) {
            return null;
        }
    }

    private void eliminarTodasLasImagenesDeCloudinary(Producto producto) {
        try {
            String json = producto.getImagen();
            if (json != null && !json.equals("[]")) {
                List<String> urls = objectMapper.readValue(json, new TypeReference<List<String>>() {});
                for (String url : urls) {
                    String publicId = obtenerPublicId(url);
                    if (publicId != null) {
                        cloudinaryService.eliminarImagen(publicId, ObjectUtils.emptyMap());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error borrando imágenes de Cloudinary: " + e.getMessage());
        }
    }

    @Override
    public List<Object[]> findTop5ProductosMasVendidos() {
        return productoRepository.findTop5ProductosMasVendidos();
    }
}
