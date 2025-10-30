package com.example.acceso.controller;

import com.example.acceso.model.Categoria;
import com.example.acceso.model.Producto;
import com.example.acceso.service.CategoriaService;
import com.example.acceso.service.ProductoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador para gestionar las operaciones CRUD de los productos.
 *
 * Proporciona endpoints para la vista de gestión de productos y una API REST
 * para interactuar con los datos de productos, incluyendo la carga de imágenes.
 */
@Controller
@RequestMapping("/productos")
public class ProductoController {
    private final ProductoService productoService;
    private final CategoriaService categoriaService;

    /**
     * Constructor para la inyección de dependencias de los servicios de producto y categoría.
     *
     * @param productoService  El servicio que maneja la lógica de negocio de los productos.
     * @param categoriaService El servicio que maneja la lógica de negocio de las categorías.
     */
    public ProductoController(ProductoService productoService, CategoriaService categoriaService) {
        this.productoService = productoService;
        this.categoriaService = categoriaService;
    }

    /**
     * Muestra la página de gestión de productos.
     *
     * @param model El modelo para pasar datos a la vista.
     * @return El nombre de la vista "productos".
     */
    @GetMapping("/listar")
    public String listarProductos(Model model) {
        List<Producto> productos = productoService.listarProductos();
        model.addAttribute("productos", productos);
        model.addAttribute("formProducto", new Producto());
        return "productos";
    }

    /**
     * Endpoint de la API para obtener todos los productos.
     *
     * @return Un {@link ResponseEntity} con la lista de productos en formato JSON.
     */
    @GetMapping("/api/listar")
    @ResponseBody
    public ResponseEntity<?> listarProductosApi() {
        Map<String, Object> response = new HashMap<>();
        List<Producto> productos = productoService.listarProductos();
        response.put("success", true);
        response.put("data", productos);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de la API para obtener todas las categorías activas.
     *
     * @return Un {@link ResponseEntity} con la lista de categorías activas.
     */
    @GetMapping("/api/categorias")
    @ResponseBody
    public ResponseEntity<?> listarCategoriasActivas() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", categoriaService.listarCategoriasActivas());
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de la API para guardar o actualizar un producto.
     * Este método maneja datos de formulario multipart, incluyendo una imagen opcional.
     *
     * @param id             El ID del producto a actualizar (opcional, nulo para creación).
     * @param nombre         El nombre del producto.
     * @param descripcion    La descripción del producto.
     * @param precioCompra   El precio de compra del producto.
     * @param precioVenta    El precio de venta del producto.
     * @param stock          La cantidad en stock del producto.
     * @param stockSeguridad El nivel mínimo de stock de seguridad.
     * @param categoriaId    El ID de la categoría a la que pertenece el producto.
     * @param imagen         El archivo de imagen del producto (opcional).
     * @return Un {@link ResponseEntity} con el resultado de la operación.
     */
    @PostMapping("/api/guardar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> guardarProductoApi(
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam("nombre") String nombre,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("precioCompra") Double precioCompra,
            @RequestParam("precioVenta") Double precioVenta,
            @RequestParam("stock") Integer stock,
            @RequestParam("stockSeguridad") Integer stockSeguridad,
            @RequestParam("id_categoria") Long categoriaId,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Producto producto;

            if (id != null) {
                producto = productoService.obtenerProductoPorId(id)
                        .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
            } else {
                producto = new Producto();
            }

            // Seteamos los valores
            producto.setNombre(nombre);
            producto.setDescripcion(descripcion);
            producto.setPrecioCompra(precioCompra);
            producto.setPrecioVenta(precioVenta);
            producto.setStock(stock);
            producto.setStockSeguridad(stockSeguridad);

            // Asignamos categoría
            Categoria categoria = categoriaService.obtenerCategoriaPorId(categoriaId)
                    .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));
            producto.setCategoria(categoria);

            Producto productoGuardado = productoService.guardarProducto(producto, imagen);

            response.put("success", true);
            response.put("producto", productoGuardado);
            response.put("message", id != null ? "Producto actualizado correctamente" : "Producto creado correctamente");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al guardar el producto: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    /**
     * Endpoint de la API para obtener un producto por su ID.
     *
     * @param id El ID del producto a obtener.
     * @return Un {@link ResponseEntity} con los datos del producto o un estado 404 si no se encuentra.
     */
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> obtenerProducto(@PathVariable Long id) {
        try {
            return productoService.obtenerProductoPorId(id).map(producto -> {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", producto);
                return ResponseEntity.ok(response);
            }).orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al obtener el producto: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Endpoint de la API para realizar el borrado lógico de un producto.
     *
     * @param id El ID del producto a eliminar.
     * @return Un {@link ResponseEntity} con el resultado de la operación.
     */
    @DeleteMapping("/api/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<?> eliminarProductoAjax(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (!productoService.obtenerProductoPorId(id).isPresent()) {
                response.put("success", false);
                response.put("message", "Producto no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            productoService.eliminarProducto(id);
            response.put("success", true);
            response.put("message", "Producto eliminado correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al eliminar el producto: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Endpoint de la API para cambiar el estado (activo/inactivo) de un producto.
     *
     * @param id El ID del producto cuyo estado se va a cambiar.
     * @return Un {@link ResponseEntity} con el producto actualizado o un error si no se encuentra.
     */
    @PostMapping("/api/cambiar-estado/{id}")
    @ResponseBody
    public ResponseEntity<?> cambiarEstadoProductoAjax(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            return productoService.cambiarEstadoProducto(id)
                    .map(producto -> {
                        response.put("success", true);
                        response.put("producto", producto);
                        response.put("message", "Estado del producto actualizado correctamente");
                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> {
                        response.put("success", false);
                        response.put("message", "Producto no encontrado");
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                    });
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al cambiar el estado del producto: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

}
