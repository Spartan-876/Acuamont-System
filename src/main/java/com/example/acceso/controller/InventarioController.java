package com.example.acceso.controller;

import com.example.acceso.DTO.AjusteInventarioDTO;
import com.example.acceso.model.AjusteInventario;
import com.example.acceso.model.Producto;
import com.example.acceso.model.TipoMovimiento;
import com.example.acceso.model.Venta;
import com.example.acceso.service.Implements.AjusteInventarioServiceImpl;
import com.example.acceso.service.Implements.InventarioServiceImpl;
import com.example.acceso.service.Implements.ProductoServiceImpl;
import com.example.acceso.service.Interfaces.AjusteInventarioService;
import com.example.acceso.service.Interfaces.InventarioService;
import com.example.acceso.service.Interfaces.ProductoService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador para la gestión del inventario.
 *
 * Proporciona endpoints para la vista de gestión de inventario y una API REST
 * para listar productos, consultar movimientos, ver ajustes y registrar nuevos
 * ajustes de inventario.
 */
@Controller
@RequestMapping("/inventario")
public class InventarioController {

    private final ProductoService productoService;
    private final InventarioService inventarioService;
    private final AjusteInventarioService ajusteInventarioService;

    /**
     * Constructor para la inyección de dependencias de los servicios necesarios.
     *
     * @param productoService         Servicio para gestionar la lógica de negocio de los productos.
     * @param inventarioService       Servicio para gestionar la lógica de negocio del inventario.
     * @param ajusteInventarioService Servicio para gestionar los ajustes de inventario.
     */
    public InventarioController(ProductoService productoService , InventarioService inventarioService, AjusteInventarioService ajusteInventarioService) {
        this.productoService = productoService;
        this.inventarioService = inventarioService;
        this.ajusteInventarioService = ajusteInventarioService;
    }

    /**
     * Muestra la página de gestión de inventario.
     *
     * @param model El modelo para pasar datos a la vista.
     * @return El nombre de la vista "inventario".
     */
    @GetMapping("/listar")
    public String listarProductos(Model model) {
        List<Producto> productos = productoService.listarProductos();
        model.addAttribute("inventario", productos);
        model.addAttribute("formProducto", new Producto());
        return "inventario";
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
     * Endpoint de la API para obtener los movimientos de un producto específico.
     * Los movimientos se derivan de las ventas.
     *
     * @param productoId El ID del producto del cual se quieren obtener los movimientos.
     * @return Un {@link ResponseEntity} con la lista de movimientos (ventas).
     */
    @GetMapping("/api/movimientos/{productoId}")
    public ResponseEntity<?> obtenerMovimientos(@PathVariable Long productoId) {
        List<Venta> movimientos = inventarioService.listarMovimientosPorProducto(productoId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", movimientos);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de la API para obtener los ajustes de inventario de un producto específico.
     *
     * @param productoId El ID del producto del cual se quieren obtener los ajustes.
     * @return Un {@link ResponseEntity} con la lista de ajustes de inventario.
     */
    @GetMapping("/api/ajustes/{productoId}")
    public ResponseEntity<?> obtenerAjustes(@PathVariable Long productoId) {
        List<AjusteInventario> ajustes = ajusteInventarioService.listarAjustePorProducto(productoId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", ajustes);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de la API para guardar un nuevo ajuste de inventario.
     *
     * @param ajusteInventarioDTO El DTO con la información del ajuste a crear.
     * @return Un {@link ResponseEntity} con el ajuste de inventario creado.
     */
    @PostMapping("/api/guardarAjuste")
    public ResponseEntity<?> guardarAjuste(@RequestBody AjusteInventarioDTO ajusteInventarioDTO) {
        Map<String, Object> response = new HashMap<>();
        AjusteInventario nuevoAjuste = ajusteInventarioService.guardarAjuste(ajusteInventarioDTO);
        response.put("success", true);
        response.put("data", nuevoAjuste);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de la API para obtener todos los tipos de movimientos de ajuste disponibles.
     *
     * @return Un {@link ResponseEntity} con la lista de tipos de movimiento.
     */
    @GetMapping("/api/tipoMovimientos")
    public ResponseEntity<?> obtenerTiposMovimientos() {
        List<TipoMovimiento> tiposMovimientos = inventarioService.listarTiposMovimientos();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", tiposMovimientos);
        return ResponseEntity.ok(response);
    }

}
