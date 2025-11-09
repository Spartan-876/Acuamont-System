package com.example.acceso.service.Interfaces;

import com.example.acceso.DTO.PagosDTO;
import com.example.acceso.DTO.VentaDTO;
import com.example.acceso.model.Cuota;
import com.example.acceso.model.Pago;
import com.example.acceso.model.Venta;

import java.math.BigDecimal;
import java.util.List;

public interface VentaService {

    List<Venta> listarVentas();

    Venta obtenerVenta(Long ventaId);

    Venta crearVenta(VentaDTO ventaRequest);

    Venta anularVenta(Long ventaId);

    Venta reemplazarVenta(Long ventaIdAntigua, VentaDTO nuevosDatosVenta);

    Venta registrarPago(PagosDTO pagoRequest);

    public List<Cuota> obtenerCuotasPorVenta(Long ventaId);

    public List<Pago> obtenerPagosPorVenta(Long ventaId);

    BigDecimal totalVentasDelDia();

    BigDecimal totalVentasDelMes();

    BigDecimal totalDeuda();

}
