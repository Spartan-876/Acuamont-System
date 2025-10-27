package com.example.acceso.service;

import com.example.acceso.model.TipoMovimiento;
import com.example.acceso.model.Venta;
import com.example.acceso.repository.TipoMovimeintoRepository;
import com.example.acceso.repository.VentaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventarioService {

    private final VentaRepository ventaRepository;
    private final TipoMovimeintoRepository tipoMovimeintoRepository;

    public InventarioService(VentaRepository ventaRepository, TipoMovimeintoRepository tipoMovimeintoRepository) {
        this.ventaRepository = ventaRepository;
        this.tipoMovimeintoRepository = tipoMovimeintoRepository;
    }

    public List<Venta> listarMovimientosPorProducto(Long productoId) {
        return ventaRepository.findVentasByProductoId(productoId);
    }

    public List<TipoMovimiento> listarTiposMovimientos() {
        return tipoMovimeintoRepository.findAllByEstadoNot(2);
    }

}
