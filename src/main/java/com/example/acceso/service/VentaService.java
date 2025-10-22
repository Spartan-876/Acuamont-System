package com.example.acceso.service;

import com.example.acceso.DTO.CuotasProgramadasDTO;
import com.example.acceso.DTO.DetalleVentaDTO;
import com.example.acceso.DTO.PagosDTO;
import com.example.acceso.DTO.VentaDTO;
import com.example.acceso.model.*;
import com.example.acceso.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final CuotaRepository cuotaRepository;
    private final PagoRepository pagoRepository;
    private final SerieComprobanteRepository serieComprobanteRepository;
    private final FormaPagoRepository formaPagoRepository;
    private final ProductoRepository productoRepository;

    public VentaService(VentaRepository ventaRepository, ClienteRepository clienteRepository, UsuarioRepository usuarioRepository, CuotaRepository cuotaRepository, PagoRepository pagoRepository, SerieComprobanteRepository serieComprobanteRepository, FormaPagoRepository formaPagoRepository, ProductoRepository productoRepository) {
        this.ventaRepository = ventaRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.cuotaRepository = cuotaRepository;
        this.pagoRepository = pagoRepository;
        this.serieComprobanteRepository = serieComprobanteRepository;
        this.formaPagoRepository = formaPagoRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<Venta> listarVentas() {
        return ventaRepository.findAllByEstadoNot(2);
    }

    @Transactional(readOnly = true)
    public Venta obtenerVenta(Long ventaId) {
        return ventaRepository.findById(ventaId)
                .orElseThrow(() -> new RuntimeException("Error: La venta con ID " + ventaId + " no existe."));
    }

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
                    .orElseThrow(() -> new RuntimeException("Error: El producto con ID " + detalleDTO.getProductoId() + " no existe."));

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
        }
        else if ("Credito".equalsIgnoreCase(formaPago.getNombre())) {
            validarConsistenciaCredito(ventaRequest, totalVenta);
            BigDecimal montoInicial = ventaRequest.getMontoInicial() != null ? ventaRequest.getMontoInicial() : BigDecimal.ZERO;
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


    private void validarConsistenciaCredito(VentaDTO ventaRequest, BigDecimal totalVenta) {
        BigDecimal montoInicial = ventaRequest.getMontoInicial() != null ? ventaRequest.getMontoInicial() : BigDecimal.ZERO;

        if (ventaRequest.getPlanDeCuotas() == null || ventaRequest.getPlanDeCuotas().isEmpty()) {
            throw new IllegalArgumentException("Una venta a crédito debe tener un plan de cuotas.");
        }

        BigDecimal sumaCuotas = ventaRequest.getPlanDeCuotas().stream()
                .map(CuotasProgramadasDTO::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPagado = montoInicial.add(sumaCuotas);

        if (totalVenta.setScale(2, RoundingMode.HALF_UP).compareTo(totalPagado.setScale(2, RoundingMode.HALF_UP)) != 0) {
            throw new IllegalArgumentException(
                    "Error de consistencia: El total de la venta (S/ " + totalVenta +
                            ") no coincide con la suma del monto inicial y las cuotas (S/ " + totalPagado + ")."
            );
        }
    }

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

    @Transactional
    public Venta reemplazarVenta(Long ventaIdAntigua, VentaDTO nuevosDatosVenta) {
        this.anularVenta(ventaIdAntigua);
        return this.crearVenta(nuevosDatosVenta);
    }

    @Transactional
    public Venta registrarPago(PagosDTO pagoRequest) {
        Cuota cuota = cuotaRepository.findById(pagoRequest.getCuotaId())
                .orElseThrow(() -> new RuntimeException("Error: La cuota con ID " + pagoRequest.getCuotaId() + " no existe."));

        if (pagoRequest.getMontoPagado().compareTo(cuota.getSaldo()) > 0) {
            throw new IllegalArgumentException("El monto a pagar no puede ser mayor que el saldo de la cuota (S/ " + cuota.getSaldo() + ").");
        }

        Pago nuevoPago = new Pago();
        nuevoPago.setCuota(cuota);
        nuevoPago.setMontoPagado(pagoRequest.getMontoPagado());
        nuevoPago.setMetodoPago(pagoRequest.getMetodoPago());
        nuevoPago.setFechaPago(LocalDateTime.now());
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

    public List<Cuota> obtenerCuotasPorVenta(Long ventaId) {
        // Validamos que la venta exista primero para dar un error claro.
        if (!ventaRepository.existsById(ventaId)) {
            throw new RuntimeException("Error: La venta con ID " + ventaId + " no existe.");
        }
        return cuotaRepository.findByVentaId(ventaId);
    }

    public List<Pago> obtenerPagosPorVenta(Long ventaId) {
        if (!ventaRepository.existsById(ventaId)) {
            throw new RuntimeException("Error: La venta con ID " + ventaId + " no existe.");
        }
        return pagoRepository.findPagosByVentaId(ventaId);
    }

    @Transactional(readOnly = true)
    public BigDecimal totalVentasDelDia() {
           List<Venta> ventas = ventaRepository.findAllByEstadoNot(2);
           if (ventas.isEmpty()) {
               return BigDecimal.ZERO;
           }
           BigDecimal total = BigDecimal.ZERO;
           for (Venta venta : ventas) {
               if (venta.getFecha().toLocalDate().isEqual(LocalDate.now())) {
                   total = total.add(venta.getTotal());
               }
           }
           return total;
    }

    @Transactional(readOnly = true)
    public BigDecimal totalVentasDelMes() {
           List<Venta> ventas = ventaRepository.findAllByEstadoNot(2);
           if (ventas.isEmpty()) {
               return BigDecimal.ZERO;
           }
           BigDecimal total = BigDecimal.ZERO;
           for (Venta venta : ventas) {
               if (venta.getFecha().toLocalDate().getMonth().equals(LocalDate.now().getMonth())) {
                   total = total.add(venta.getTotal());
               }
           }
           return total;
    }

    @Transactional(readOnly = true)
    public BigDecimal totalDeuda() {
        List<Venta> ventas = ventaRepository.findAllByEstadoNot(2);
        if (ventas.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (Venta venta : ventas) {
            total = total.add(venta.getDeuda());
        }
        return total;
    }

    
}
