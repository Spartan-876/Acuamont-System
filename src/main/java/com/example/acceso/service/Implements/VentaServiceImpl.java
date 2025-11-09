package com.example.acceso.service.Implements;

import com.example.acceso.DTO.CuotasProgramadasDTO;
import com.example.acceso.DTO.DetalleVentaDTO;
import com.example.acceso.DTO.PagosDTO;
import com.example.acceso.DTO.VentaDTO;
import com.example.acceso.model.*;
import com.example.acceso.repository.*;
import com.example.acceso.service.Interfaces.VentaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para gestionar la lógica de negocio de las ventas.
 *
 * Proporciona métodos para crear, anular, consultar y gestionar pagos de
 * ventas,
 * manejando la lógica de negocio como la actualización de stock, generación de
 * cuotas,
 * y cálculo de totales.
 */
@Service
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final CuotaRepository cuotaRepository;
    private final PagoRepository pagoRepository;
    private final SerieComprobanteRepository serieComprobanteRepository;
    private final FormaPagoRepository formaPagoRepository;
    private final ProductoRepository productoRepository;

    /**
     * Constructor para la inyección de todas las dependencias de repositorios
     * necesarias.
     *
     * @param ventaRepository            Repositorio para las operaciones de Venta.
     * @param clienteRepository          Repositorio para las operaciones de
     *                                   Cliente.
     * @param usuarioRepository          Repositorio para las operaciones de
     *                                   Usuario.
     * @param cuotaRepository            Repositorio para las operaciones de Cuota.
     * @param pagoRepository             Repositorio para las operaciones de Pago.
     * @param serieComprobanteRepository Repositorio para las operaciones de
     *                                   SerieComprobante.
     * @param formaPagoRepository        Repositorio para las operaciones de
     *                                   FormaPago.
     * @param productoRepository         Repositorio para las operaciones de
     *                                   Producto.
     */
    public VentaServiceImpl(VentaRepository ventaRepository, ClienteRepository clienteRepository,
                            UsuarioRepository usuarioRepository, CuotaRepository cuotaRepository, PagoRepository pagoRepository,
                            SerieComprobanteRepository serieComprobanteRepository, FormaPagoRepository formaPagoRepository,
                            ProductoRepository productoRepository) {
        this.ventaRepository = ventaRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.cuotaRepository = cuotaRepository;
        this.pagoRepository = pagoRepository;
        this.serieComprobanteRepository = serieComprobanteRepository;
        this.formaPagoRepository = formaPagoRepository;
        this.productoRepository = productoRepository;
    }

    /**
     * Obtiene una lista de todas las ventas que no están anuladas (estado != 2).
     *
     * @return Una lista de objetos {@link Venta}.
     */
    @Transactional(readOnly = true)
    public List<Venta> listarVentas() {
        return ventaRepository.findAllByEstadoNot(2);
    }

    /**
     * Busca una venta por su ID.
     *
     * @param ventaId El ID de la venta a buscar.
     * @return La entidad {@link Venta} encontrada.
     * @throws RuntimeException si no se encuentra una venta con el ID
     *                          proporcionado.
     */
    @Transactional(readOnly = true)
    public Venta obtenerVenta(Long ventaId) {
        return ventaRepository.findById(ventaId)
                .orElseThrow(() -> new RuntimeException("Error: La venta con ID " + ventaId + " no existe."));
    }

    /**
     * Crea una nueva venta a partir de los datos proporcionados en un DTO.
     * Actualiza el stock de los productos, genera cuotas si es a crédito y
     * actualiza el correlativo de la serie.
     *
     * @param ventaRequest El DTO {@link VentaDTO} con todos los datos de la venta.
     * @return La entidad {@link Venta} creada y guardada en la base de datos.
     * @throws RuntimeException         si alguna entidad relacionada no existe o si
     *                                  no hay stock suficiente.
     * @throws IllegalArgumentException si hay inconsistencias en los datos de una
     *                                  venta a crédito.
     */
    @Transactional
    public Venta crearVenta(VentaDTO ventaRequest) {

        Cliente cliente = clienteRepository.findById(ventaRequest.getClienteId())
                .orElseThrow(() -> new RuntimeException("Error: El cliente no existe."));
        Usuario usuario = usuarioRepository.findById(ventaRequest.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Error: El usuario no existe."));
        FormaPago formaPago = formaPagoRepository.findById(ventaRequest.getFormaPagoId())
                .orElseThrow(() -> new RuntimeException("Error: La forma de pago no existe."));
        SerieComprobante serie = serieComprobanteRepository.findById(ventaRequest.getSerieComprobanteId())
                .orElseThrow(() -> new RuntimeException("Error: La serie de comprobante no existe."));

        BigDecimal totalVenta = BigDecimal.ZERO;
        Venta nuevaVenta = new Venta();

        for (DetalleVentaDTO detalleDTO : ventaRequest.getDetalles()) {
            Producto producto = productoRepository.findById(detalleDTO.getProductoId())
                    .orElseThrow(() -> new RuntimeException(
                            "Error: El producto con ID " + detalleDTO.getProductoId() + " no existe."));

            int cantidad = detalleDTO.getCantidad();

            if (producto.getStock() < cantidad) {
                throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre());
            }

            producto.setStock(producto.getStock() - cantidad);

            DetalleVenta nuevoDetalle = new DetalleVenta();
            nuevoDetalle.setVenta(nuevaVenta);
            nuevoDetalle.setProducto(producto);
            nuevoDetalle.setCantidad(cantidad);
            nuevoDetalle.setPrecioUnitario(BigDecimal.valueOf(producto.getPrecioVenta()));

            BigDecimal subtotal = BigDecimal.valueOf(producto.getPrecioVenta()).multiply(BigDecimal.valueOf(cantidad));
            nuevoDetalle.setSubtotal(subtotal);

            nuevaVenta.getDetalleVentas().add(nuevoDetalle);
            totalVenta = totalVenta.add(subtotal);
        }

        nuevaVenta.setCliente(cliente);
        nuevaVenta.setUsuario(usuario);
        nuevaVenta.setFormaPago(formaPago);
        nuevaVenta.setSerieComprobante(serie);
        nuevaVenta.setCorrelativo(serie.getCorrelativo_actual() + 1);
        nuevaVenta.setFecha(LocalDateTime.now());
        nuevaVenta.setTotal(totalVenta.setScale(2, RoundingMode.HALF_UP));

        if ("Contado".equalsIgnoreCase(formaPago.getNombre())) {
            nuevaVenta.setDeuda(BigDecimal.ZERO);
            nuevaVenta.setEstado(1); // Pagada
        } else if ("Credito".equalsIgnoreCase(formaPago.getNombre())) {
            validarConsistenciaCredito(ventaRequest, totalVenta);
            BigDecimal montoInicial = ventaRequest.getMontoInicial() != null ? ventaRequest.getMontoInicial()
                    : BigDecimal.ZERO;
            BigDecimal deudaReal = totalVenta.subtract(montoInicial);
            nuevaVenta.setDeuda(deudaReal);
            nuevaVenta.setEstado(0);// Pendiente

            for (CuotasProgramadasDTO cuotaDTO : ventaRequest.getPlanDeCuotas()) {
                Cuota cuota = new Cuota();
                cuota.setVenta(nuevaVenta);
                cuota.setNumeroCuota(nuevaVenta.getCuotas().size() + 1);
                cuota.setMonto(cuotaDTO.getMonto());
                cuota.setSaldo(cuotaDTO.getMonto());
                cuota.setFechaVencimiento(cuotaDTO.getFechaVencimiento());
                cuota.setEstado(0); // Pendiente
                nuevaVenta.getCuotas().add(cuota);
            }

        }

        Venta ventaGuardada = ventaRepository.save(nuevaVenta);

        serie.setCorrelativo_actual(serie.getCorrelativo_actual() + 1);

        return ventaGuardada;
    }

    /**
     * Valida la consistencia de los montos en una venta a crédito.
     * Asegura que el total de la venta sea igual a la suma del monto inicial más
     * todas las cuotas.
     *
     * @param ventaRequest El DTO de la venta que contiene el plan de cuotas y el
     *                     monto inicial.
     * @param totalVenta   El total calculado de la venta.
     * @throws IllegalArgumentException si los montos no coinciden o si no hay plan
     *                                  de cuotas.
     */
    private void validarConsistenciaCredito(VentaDTO ventaRequest, BigDecimal totalVenta) {
        BigDecimal montoInicial = ventaRequest.getMontoInicial() != null ? ventaRequest.getMontoInicial()
                : BigDecimal.ZERO;

        if (ventaRequest.getPlanDeCuotas() == null || ventaRequest.getPlanDeCuotas().isEmpty()) {
            throw new IllegalArgumentException("Una venta a crédito debe tener un plan de cuotas.");
        }

        BigDecimal sumaCuotas = ventaRequest.getPlanDeCuotas().stream()
                .map(CuotasProgramadasDTO::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPagado = montoInicial.add(sumaCuotas);

        if (totalVenta.setScale(2, RoundingMode.HALF_UP)
                .compareTo(totalPagado.setScale(2, RoundingMode.HALF_UP)) != 0) {
            throw new IllegalArgumentException(
                    "Error de consistencia: El total de la venta (S/ " + totalVenta +
                            ") no coincide con la suma del monto inicial y las cuotas (S/ " + totalPagado + ").");
        }
    }

    /**
     * Anula una venta existente.
     * Cambia el estado de la venta y sus cuotas a "anulado" (2) y revierte el stock
     * de los productos vendidos.
     *
     * @param ventaId El ID de la venta a anular.
     * @return La entidad {@link Venta} con su estado actualizado a "anulado".
     * @throws RuntimeException      si la venta no se encuentra.
     * @throws IllegalStateException si la venta ya ha sido anulada previamente.
     */
    @Transactional
    public Venta anularVenta(Long ventaId) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new RuntimeException("Error: La venta con ID " + ventaId + " no existe."));

        if (venta.getEstado() == 2) {
            throw new IllegalStateException("La venta ya se encuentra eliminada y no puede ser procesada de nuevo.");
        }

        for (DetalleVenta detalle : venta.getDetalleVentas()) {
            Producto producto = detalle.getProducto();
            int cantidadVendida = detalle.getCantidad();

            producto.setStock(producto.getStock() + cantidadVendida);
        }

        venta.setEstado(2);

        for (Cuota cuota : venta.getCuotas()) {
            if (cuota.getEstado() != 2) {
                cuota.setEstado(2);
                cuota.setSaldo(BigDecimal.ZERO);
            }
        }

        ventaRepository.save(venta);
        return venta;
    }

    /**
     * Reemplaza una venta existente anulando la antigua y creando una nueva.
     * Este método es útil para la funcionalidad de "editar" una venta, asegurando
     * la consistencia transaccional.
     *
     * @param ventaIdAntigua   El ID de la venta a anular.
     * @param nuevosDatosVenta El DTO con los datos de la nueva venta que la
     *                         reemplazará.
     * @return La nueva entidad {@link Venta} creada.
     */
    @Transactional
    public Venta reemplazarVenta(Long ventaIdAntigua, VentaDTO nuevosDatosVenta) {
        this.anularVenta(ventaIdAntigua);
        return this.crearVenta(nuevosDatosVenta);
    }

    /**
     * Registra un pago para una cuota específica de una venta.
     * Actualiza el saldo de la cuota y la deuda total de la venta.
     *
     * @param pagoRequest El DTO {@link PagosDTO} con los detalles del pago.
     * @return La entidad {@link Venta} actualizada después de registrar el pago.
     * @throws RuntimeException         si la cuota no se encuentra.
     * @throws IllegalArgumentException si el monto pagado es mayor que el saldo de
     *                                  la cuota.
     */
    @Transactional
    public Venta registrarPago(PagosDTO pagoRequest) {
        Cuota cuota = cuotaRepository.findById(pagoRequest.getCuotaId())
                .orElseThrow(() -> new RuntimeException(
                        "Error: La cuota con ID " + pagoRequest.getCuotaId() + " no existe."));

        if (pagoRequest.getMontoPagado().compareTo(cuota.getSaldo()) > 0) {
            throw new IllegalArgumentException(
                    "El monto a pagar no puede ser mayor que el saldo de la cuota (S/ " + cuota.getSaldo() + ").");
        }

        Pago nuevoPago = new Pago();
        nuevoPago.setCuota(cuota);
        nuevoPago.setMontoPagado(pagoRequest.getMontoPagado());
        nuevoPago.setMetodoPago(pagoRequest.getMetodoPago());
        nuevoPago.setFechaPago(LocalDateTime.now());
        nuevoPago.setComentario(pagoRequest.getComentario());
        pagoRepository.save(nuevoPago);

        cuota.setSaldo(cuota.getSaldo().subtract(pagoRequest.getMontoPagado()));
        if (cuota.getSaldo().compareTo(BigDecimal.ZERO) == 0) {
            cuota.setEstado(1); // Pagada
        }

        Venta venta = cuota.getVenta();
        venta.setDeuda(venta.getDeuda().subtract(pagoRequest.getMontoPagado()));

        if (venta.getDeuda().compareTo(BigDecimal.ZERO) <= 0) {
            venta.setEstado(1); // Pagada
        }

        return venta;
    }

    /**
     * Obtiene todas las cuotas asociadas a una venta específica.
     *
     * @param ventaId El ID de la venta.
     * @return Una lista de objetos {@link Cuota}.
     */
    public List<Cuota> obtenerCuotasPorVenta(Long ventaId) {
        // Validamos que la venta exista primero para dar un error claro.
        if (!ventaRepository.existsById(ventaId)) {
            throw new RuntimeException("Error: La venta con ID " + ventaId + " no existe.");
        }
        return cuotaRepository.findByVentaId(ventaId);
    }

    /**
     * Obtiene todos los pagos realizados para una venta específica.
     *
     * @param ventaId El ID de la venta.
     * @return Una lista de objetos {@link Pago}.
     */
    public List<Pago> obtenerPagosPorVenta(Long ventaId) {
        if (!ventaRepository.existsById(ventaId)) {
            throw new RuntimeException("Error: La venta con ID " + ventaId + " no existe.");
        }
        return pagoRepository.findPagosByVentaId(ventaId);
    }

    /**
     * Calcula el total de ventas realizadas en el día actual.
     *
     * @return Un {@link BigDecimal} con la suma total de las ventas del día.
     */
    @Transactional(readOnly = true)
    public BigDecimal totalVentasDelDia() {
        
        return ventaRepository.sumTotalVentasDelDia();
    }

    /**
     * Calcula el total de ventas realizadas en el mes actual.
     *
     * @return Un {@link BigDecimal} con la suma total de las ventas del mes.
     */
    @Transactional(readOnly = true)
    public BigDecimal totalVentasDelMes() {
        
        return ventaRepository.sumTotalVentasDelMes();
    }

    /**
     * Calcula la deuda total pendiente de todas las ventas a crédito.
     *
     * @return Un {@link BigDecimal} con la suma total de las deudas.
     */
    @Transactional(readOnly = true)
    public BigDecimal totalDeuda() {
        
        return ventaRepository.sumTotalDeuda();
    }

}
