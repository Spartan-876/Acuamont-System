package com.example.acceso.controller;

import com.example.acceso.DTO.VentaDTO;
import com.example.acceso.model.Cuota;
import com.example.acceso.model.Pago;
import com.example.acceso.model.Venta;
import com.example.acceso.service.Interfaces.FormaPagoService;
import com.example.acceso.service.Interfaces.GenerarBoletaService;
import com.example.acceso.service.Interfaces.SerieComprobanteService;
import com.example.acceso.service.Interfaces.VentaService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador para gestionar las operaciones de ventas.
 *
 * Proporciona endpoints para la vista de gestión de ventas y una API REST
 * para crear, listar, actualizar, anular y consultar ventas, así como
 * sus datos relacionados como cuotas y pagos.
 */
@Controller
@RequestMapping("/ventas")
public class VentaController {


    private GenerarBoletaService generarBoletaService;
    private final VentaService ventaService;
    private final FormaPagoService formaPagoService;
    private final SerieComprobanteService serieComprobanteService;

    /**
     * Constructor para la inyección de dependencias de los servicios necesarios.
     *
     * @param ventaService            Servicio para la lógica de negocio de las
     *                                ventas.
     * @param formaPagoService        Servicio para obtener las formas de pago.
     * @param serieComprobanteService Servicio para obtener las series de
     *                                comprobantes.
     */
    public VentaController(VentaService ventaService, FormaPagoService formaPagoService,
                           SerieComprobanteService serieComprobanteService,GenerarBoletaService generarBoletaService) {
        this.generarBoletaService = generarBoletaService;
        this.ventaService = ventaService;
        this.formaPagoService = formaPagoService;
        this.serieComprobanteService = serieComprobanteService;
    }

    /**
     * Muestra la página de gestión de ventas.
     *
     * @param model El modelo para pasar datos a la vista.
     * @return El nombre de la vista "ventas".
     */
    @GetMapping("/listar")
    public String listarVentas(Model model) {
        List<Venta> ventas = ventaService.listarVentas();
        model.addAttribute("ventas", ventas);
        model.addAttribute("formVenta", new Venta());
        return "ventas";
    }

    /**
     * Endpoint de la API para obtener todas las ventas.
     *
     * @return Un {@link ResponseEntity} con la lista de ventas en formato JSON.
     */
    @GetMapping("/api/listar")
    @ResponseBody
    public ResponseEntity<?> listarVentasApi() {
        Map<String, Object> response = new HashMap<>();
        List<Venta> ventas = ventaService.listarVentas();
        response.put("success", true);
        response.put("data", ventas);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de la API para obtener una venta específica por su ID.
     *
     * @param id El ID de la venta a obtener.
     * @return Un {@link ResponseEntity} con los datos de la venta o un estado 404
     *         si no se encuentra.
     */
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

    /**
     * Endpoint de la API para obtener todas las formas de pago disponibles.
     *
     * @return Un {@link ResponseEntity} con la lista de formas de pago.
     */
    @GetMapping("/api/formaPago")
    @ResponseBody
    public ResponseEntity<?> listarFormasPagoApi() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", formaPagoService.listarFormasPago());
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de la API para obtener todas las series de comprobantes disponibles.
     *
     * @return Un {@link ResponseEntity} con la lista de series de comprobantes.
     */
    @GetMapping("/api/serieComprobante")
    @ResponseBody
    public ResponseEntity<?> listarSeriesComprobanteApi() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", serieComprobanteService.listarSerieComprobante());
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de la API para crear una nueva venta.
     *
     * @param venta El DTO con los datos de la venta a crear, validado.
     * @return Un {@link ResponseEntity} con la venta creada y un estado 201
     *         (Created).
     */
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

    /**
     * Endpoint de la API para actualizar (reemplazar) una venta existente.
     *
     * @param id           El ID de la venta a actualizar.
     * @param ventaRequest El DTO con los nuevos datos de la venta.
     * @return Un {@link ResponseEntity} con la venta actualizada.
     */
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

    /**
     * Endpoint de la API para anular una venta.
     *
     * @param id El ID de la venta a anular.
     * @return Un {@link ResponseEntity} con la venta anulada.
     */
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

    /**
     * Endpoint de la API para obtener las cuotas de una venta específica.
     *
     * @param ventaId El ID de la venta de la cual se quieren obtener las cuotas.
     * @return Un {@link ResponseEntity} con la lista de cuotas.
     */
    @GetMapping("/api/cuotas/{ventaId}")
    @ResponseBody
    public ResponseEntity<List<Cuota>> listarCuotasPorVenta(@PathVariable Long ventaId) {
        List<Cuota> cuotas = ventaService.obtenerCuotasPorVenta(ventaId);
        return ResponseEntity.ok(cuotas);
    }

    /**
     * Endpoint de la API para obtener los pagos realizados para una venta
     * específica.
     *
     * @param ventaId El ID de la venta de la cual se quieren obtener los pagos.
     * @return Un {@link ResponseEntity} con la lista de pagos.
     */
    @GetMapping("/api/pagos/{ventaId}")
    @ResponseBody
    public ResponseEntity<List<Pago>> listarPagosPorVenta(@PathVariable Long ventaId) {
        List<Pago> pagos = ventaService.obtenerPagosPorVenta(ventaId);
        return ResponseEntity.ok(pagos);
    }

    @GetMapping("/api/boleta/{ventaId}")
    public void descargarBoletaPDF(@PathVariable Long ventaId, HttpServletResponse response) {
        try {
            byte[] pdfBytes = generarBoletaService.generarBoletaPdf(ventaId);
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=\"boleta_" + ventaId + ".pdf\"");
            OutputStream outputStream = response.getOutputStream();
            outputStream.write(pdfBytes);
            outputStream.flush();
            outputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/api/envio-correo/{ventaid}")
    public ResponseEntity<?> enviarBoletaPorCorreo(@PathVariable Long ventaid) {
        Map<String, Object> response = new HashMap<>();

        try {
            String correoEnviado = generarBoletaService.enviarBoletaPorCorreo(ventaid);
            response.put("success", true);
            response.put("message", "Boleta enviada exitosamente a: " + correoEnviado);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error al enviar correo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
