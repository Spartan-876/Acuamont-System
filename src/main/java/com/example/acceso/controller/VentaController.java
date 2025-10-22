package com.example.acceso.controller;

import com.example.acceso.DTO.VentaDTO;
import com.example.acceso.model.Cuota;
import com.example.acceso.model.Pago;
import com.example.acceso.model.Usuario;
import com.example.acceso.model.Venta;
import com.example.acceso.service.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/ventas")
public class VentaController {

    private final VentaService ventaService;
    private final UsuarioService usuarioService;
    private final FormaPagoService formaPagoService;
    private final SerieComprobanteService serieComprobanteService;

    public VentaController(VentaService ventaService, UsuarioService usuarioService, FormaPagoService formaPagoService, SerieComprobanteService serieComprobanteService) {
        this.ventaService = ventaService;
        this.usuarioService = usuarioService;
        this.formaPagoService = formaPagoService;
        this.serieComprobanteService = serieComprobanteService;
    }

    @GetMapping("/listar")
    public String listarVentas(Model model) {
        List<Venta> ventas = ventaService.listarVentas();
        model.addAttribute("ventas", ventas);
        model.addAttribute("formVenta", new Venta());
        return "ventas";
    }

    @GetMapping("/api/usuarioLogueado")
    @ResponseBody
    public ResponseEntity<?> listarUsuariosApi(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", usuarioService.listarUsuarios());

        // Obtener usuario de sesión
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioLogueado != null) {
            response.put("usuarioActual", usuarioLogueado.getId());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/listar")
    @ResponseBody
    public ResponseEntity<?> listarVentasApi() {
        Map<String, Object> response = new HashMap<>();
        List<Venta> ventas = ventaService.listarVentas();
        response.put("success", true);
        response.put("data", ventas);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/ventas_id/{id}")
    public ResponseEntity<?> obtenerVentaPorId(@PathVariable Long id) {
        try {
            Venta venta = ventaService.obtenerVenta(id);

            if (venta == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Venta no encontrada"));
            }

            return ResponseEntity.ok(Map.of("success", true, "data", venta));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error al obtener la venta", "error", e.getMessage()));
        }
    }

    @GetMapping("/api/formaPago")
    @ResponseBody
    public ResponseEntity<?> listarFormasPagoApi() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", formaPagoService.listarFormasPago());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/serieComprobante")
    @ResponseBody
    public ResponseEntity<?> listarSeriesComprobanteApi() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", serieComprobanteService.listarSerieComprobante());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/guardar")
    @ResponseBody
    public ResponseEntity<?> guardarVentaApi(@Valid @RequestBody VentaDTO venta) {
        try {
            Venta ventaGuardada = ventaService.crearVenta(venta);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Venta guardada exitosamente");
            response.put("data", ventaGuardada);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al guardar la venta: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    @PutMapping("/api/actualizar/{id}")
    @ResponseBody
    public ResponseEntity<?> actualizarVenta(@PathVariable Long id, @Valid @RequestBody VentaDTO ventaRequest) {
        try {
            Venta ventaReemplazo = ventaService.reemplazarVenta(id, ventaRequest);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Venta actualizada correctamente");
            response.put("data", ventaReemplazo);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al actualizar la venta: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }


    @DeleteMapping("/api/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<?> eliminarVentaApi(@PathVariable Long id) {
        Venta ventaEliminada = ventaService.anularVenta(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Venta anulada correctamente");
        response.put("data", ventaEliminada);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/cuotas/{ventaId}")
    @ResponseBody
    public ResponseEntity<List<Cuota>> listarCuotasPorVenta(@PathVariable Long ventaId) {
        List<Cuota> cuotas = ventaService.obtenerCuotasPorVenta(ventaId);
        return ResponseEntity.ok(cuotas);
    }

    @GetMapping("/api/pagos/{ventaId}")
    @ResponseBody
    public ResponseEntity<List<Pago>> listarPagosPorVenta(@PathVariable Long ventaId) {
        List<Pago> pagos = ventaService.obtenerPagosPorVenta(ventaId);
        return ResponseEntity.ok(pagos);
    }


}
