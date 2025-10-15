package com.example.acceso.controller;

import com.example.acceso.model.DetalleVenta;
import com.example.acceso.model.Producto;
import com.example.acceso.model.Venta;
import com.example.acceso.service.InventarioService;
import com.example.acceso.service.ProductoService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/inventario")
public class InventarioController {

    private final ProductoService productoService;
    private final InventarioService inventarioService;

    public InventarioController(ProductoService productoService , InventarioService inventarioService) {
        this.productoService = productoService;
        this.inventarioService = inventarioService;
    }

    @GetMapping("/listar")
    public String listarProductos(Model model) {
        List<Producto> productos = productoService.listarProductos();
        model.addAttribute("inventario", productos);
        model.addAttribute("formProducto", new Producto());
        return "inventario";
    }

    @GetMapping("/api/listar")
    @ResponseBody
    public ResponseEntity<?> listarProductosApi() {
        Map<String, Object> response = new HashMap<>();
        List<Producto> productos = productoService.listarProductos();
        response.put("success", true);
        response.put("data", productos);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/movimientos/{productoId}")
    public ResponseEntity<?> obtenerMovimientos(@PathVariable Long productoId) {
        List<Venta> movimientos = inventarioService.listarMovimientosPorProducto(productoId);
        return ResponseEntity.ok(movimientos);
    }

}
