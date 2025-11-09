package com.example.acceso.service.Interfaces;

import com.example.acceso.model.TipoMovimiento;
import com.example.acceso.model.Venta;

import java.util.List;

public interface InventarioService {

    List<Venta> listarMovimientosPorProducto(Long productoId);

    List<TipoMovimiento> listarTiposMovimientos();

}
