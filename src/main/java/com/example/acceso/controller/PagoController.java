package com.example.acceso.controller;

import com.example.acceso.DTO.PagosDTO;
import com.example.acceso.model.Venta;
import com.example.acceso.service.VentaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controlador para gestionar el registro de pagos asociados a una venta.
 *
 * Proporciona endpoints de API para procesar los pagos realizados por los clientes
 * sobre las cuotas de una venta a crédito.
 */
@Controller
@RequestMapping("/pagos")
public class PagoController {

    private final VentaService ventaService;

    /**
     * Constructor para la inyección de dependencias del servicio de ventas.
     *
     * @param ventaService El servicio que maneja la lógica de negocio de las ventas y pagos.
     */
    public PagoController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    /**
     * Endpoint de la API para registrar un nuevo pago para una venta existente.
     *
     * Recibe los detalles del pago, lo procesa a través del {@link VentaService} y
     * devuelve la entidad de la venta con su estado actualizado (cuotas pagadas, saldo, etc.).
     *
     * @param pagoRequest El DTO que contiene la información del pago a registrar, validado.
     * @return Un {@link ResponseEntity} con la entidad {@link Venta} actualizada después de registrar el pago.
     */
    @PostMapping("/api/registrarPago")
    public ResponseEntity<Venta> registrarPago(@Valid @RequestBody PagosDTO pagoRequest) {
        Venta ventaActualizada = ventaService.registrarPago(pagoRequest);
        return ResponseEntity.ok(ventaActualizada);
    }

}
