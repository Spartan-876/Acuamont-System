package com.example.acceso.service.Implements;

import com.example.acceso.DTO.AjusteInventarioDTO;
import com.example.acceso.model.AjusteInventario;
import com.example.acceso.model.Producto;
import com.example.acceso.model.TipoMovimiento;
import com.example.acceso.repository.AjusteInventarioRepository;
import com.example.acceso.repository.TipoMovimientoRepository;
import com.example.acceso.service.Interfaces.AjusteInventarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio para gestionar la lógica de negocio de los ajustes de inventario.
 *
 * Proporciona métodos para crear nuevos ajustes de inventario, lo que implica
 * la actualización del stock de los productos, y para listar los ajustes
 * existentes por producto.
 */
@Service
public class AjusteInventarioServiceImpl implements AjusteInventarioService {

    private final AjusteInventarioRepository ajusteInventarioRepository;
    private final TipoMovimientoRepository tipoMovimientoRepository;
    private final ProductoServiceImpl productoService;

    /**
     * Constructor para la inyección de dependencias de los repositorios y servicios
     * necesarios.
     *
     * @param ajusteInventarioRepository Repositorio para las operaciones CRUD de
     *                                   {@link AjusteInventario}.
     * @param tipoMovimientoRepository   Repositorio para las operaciones CRUD de
     *                                   {@link TipoMovimiento}.
     * @param productoService            Servicio para acceder a la lógica de
     *                                   negocio de los productos.
     */
    public AjusteInventarioServiceImpl(AjusteInventarioRepository ajusteInventarioRepository,
                                       TipoMovimientoRepository tipoMovimientoRepository, ProductoServiceImpl productoService) {
        this.ajusteInventarioRepository = ajusteInventarioRepository;
        this.tipoMovimientoRepository = tipoMovimientoRepository;
        this.productoService = productoService;
    }

    /**
     * Guarda un nuevo ajuste de inventario y actualiza el stock del producto
     * correspondiente.
     *
     * @param ajusteInventarioDTO El DTO que contiene los datos del ajuste a crear.
     * @return El objeto {@link AjusteInventario} que fue guardado en la base de
     *         datos.
     * @throws RuntimeException Si el producto o el tipo de movimiento no se
     *                          encuentran, o si no hay stock suficiente para una
     *                          salida.
     */
    @Transactional
    public AjusteInventario guardarAjuste(AjusteInventarioDTO ajusteInventarioDTO) {

        Producto producto = productoService.obtenerProductoPorId(ajusteInventarioDTO.getProductoId())
                .orElseThrow(() -> new RuntimeException(
                        "Error: Producto no encontrado con ID: " + ajusteInventarioDTO.getProductoId()));

        TipoMovimiento tipo = tipoMovimientoRepository.findById(ajusteInventarioDTO.getTipoMovimientoId())
                .orElseThrow(() -> new RuntimeException("Error: Tipo de Movimiento no encontrado con ID: "
                        + ajusteInventarioDTO.getTipoMovimientoId()));

        AjusteInventario nuevoAjuste = new AjusteInventario(producto, tipo, ajusteInventarioDTO.getCantidad(),
                ajusteInventarioDTO.getComentario());

        switch (tipo.getNombre()) {
            case "Entrada":
                nuevoAjuste.getProducto().setStock(nuevoAjuste.getProducto().getStock() + nuevoAjuste.getCantidad());
                break;
            case "Salida":
                if (producto.getStock() < nuevoAjuste.getCantidad()) {
                    throw new RuntimeException("Error: Stock insuficiente. Stock actual: " + producto.getStock());
                } else {
                    nuevoAjuste.getProducto()
                            .setStock(nuevoAjuste.getProducto().getStock() - nuevoAjuste.getCantidad());
                }
                break;
            default:
                break;
        }

        ajusteInventarioRepository.save(nuevoAjuste);
        return nuevoAjuste;

    }

    /**
     * Lista todos los ajustes de inventario asociados a un producto específico.
     *
     * @param id El ID del producto del cual se quieren listar los ajustes.
     * @return Una lista de objetos {@link AjusteInventario}.
     */
    @Transactional(readOnly = true)
    public List<AjusteInventario> listarAjustePorProducto(Long id) {
        return ajusteInventarioRepository.findByProductoId(id);
    }

}
