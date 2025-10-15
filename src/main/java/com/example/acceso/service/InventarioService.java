package com.example.acceso.service;

import com.example.acceso.model.DetalleVenta;
import com.example.acceso.model.Venta;
import com.example.acceso.repository.DetalleVentaRepository;
import com.example.acceso.repository.VentaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventarioService {

    private final VentaRepository ventaRepository;

    public InventarioService(VentaRepository ventaRepository) {
        this.ventaRepository = ventaRepository;
    }

    public List<Venta> listarMovimientosPorProducto(Long productoId) {
        return ventaRepository.findVentasByProductoId(productoId);
    }

}
