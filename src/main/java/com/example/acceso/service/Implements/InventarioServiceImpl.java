package com.example.acceso.service.Implements;

import com.example.acceso.model.TipoMovimiento;
import com.example.acceso.model.Venta;
import com.example.acceso.repository.TipoMovimientoRepository;
import com.example.acceso.repository.VentaRepository;
import com.example.acceso.service.Interfaces.InventarioService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio para gestionar la lógica de negocio relacionada con el inventario.
 *
 * Proporciona métodos para consultar los movimientos de productos (basados en ventas)
 * y para listar los tipos de movimientos de ajuste disponibles.
 */
@Service
public class InventarioServiceImpl implements InventarioService {

    private final VentaRepository ventaRepository;
    private final TipoMovimientoRepository tipoMovimeintoRepository;

    /**
     * Constructor para la inyección de dependencias de los repositorios necesarios.
     *
     * @param ventaRepository          Repositorio para acceder a los datos de las ventas.
     * @param tipoMovimeintoRepository Repositorio para acceder a los tipos de movimiento.
     */
    public InventarioServiceImpl(VentaRepository ventaRepository, TipoMovimientoRepository tipoMovimeintoRepository) {
        this.ventaRepository = ventaRepository;
        this.tipoMovimeintoRepository = tipoMovimeintoRepository;
    }

    /**
     * Lista todos los movimientos de salida de un producto específico, basados en las ventas registradas.
     *
     * @param productoId El ID del producto del cual se quieren obtener los movimientos.
     * @return Una lista de objetos {@link Venta} que representan los movimientos de salida.
     */
    public List<Venta> listarMovimientosPorProducto(Long productoId) {
        return ventaRepository.findVentasByProductoId(productoId);
    }

    /**
     * Obtiene una lista de todos los tipos de movimiento de ajuste que no están eliminados lógicamente.
     *
     * @return Una lista de objetos {@link TipoMovimiento}.
     */
    public List<TipoMovimiento> listarTiposMovimientos() {
        return tipoMovimeintoRepository.findAllByEstadoNot(2);
    }

}
