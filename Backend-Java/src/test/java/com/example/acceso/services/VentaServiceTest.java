package com.example.acceso.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.acceso.DTO.CuotasProgramadasDTO;
import com.example.acceso.DTO.DetalleVentaDTO;
import com.example.acceso.DTO.PagosDTO;
import com.example.acceso.DTO.VentaDTO;
import com.example.acceso.model.*;
import com.example.acceso.repository.*;
import com.example.acceso.service.Implements.VentaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas del Servicio VentaService")
class VentaServiceTest {

    // Simular todos los 8 repositorios inyectados
    @Mock private VentaRepository ventaRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private CuotaRepository cuotaRepository;
    @Mock private PagoRepository pagoRepository;
    @Mock private SerieComprobanteRepository serieComprobanteRepository;
    @Mock private FormaPagoRepository formaPagoRepository;
    @Mock private ProductoRepository productoRepository;

    @InjectMocks
    private VentaServiceImpl ventaService;

    // --- Mocks de objetos base para las pruebas ---
    private Cliente mockCliente;
    private Usuario mockUsuario;
    private Producto mockProducto1;
    private SerieComprobante mockSerie;
    private FormaPago mockFormaPagoContado;
    private FormaPago mockFormaPagoCredito;
    private VentaDTO mockVentaDTO;
    private DetalleVentaDTO mockDetalleDTO;

    @BeforeEach
    void setUp() {
        // Configuramos los mocks que se usarán en casi todas las pruebas
        mockCliente = new Cliente();
        mockCliente.setId(1L);
        mockCliente.setNombre("Cliente Prueba");

        mockUsuario = new Usuario();
        mockUsuario.setId(1L);
        mockUsuario.setNombre("Usuario Prueba");

        mockProducto1 = new Producto();
        mockProducto1.setId(1L);
        mockProducto1.setNombre("Producto 1");
        mockProducto1.setStock(100);
        mockProducto1.setPrecioVenta(50.0);

        mockSerie = new SerieComprobante();
        mockSerie.setId(1L);
        mockSerie.setCorrelativo_actual(99);

        mockFormaPagoContado = new FormaPago();
        mockFormaPagoContado.setId(1L);
        mockFormaPagoContado.setNombre("Contado");

        mockFormaPagoCredito = new FormaPago();
        mockFormaPagoCredito.setId(2L);
        mockFormaPagoCredito.setNombre("Credito");

        mockDetalleDTO = new DetalleVentaDTO();
        mockDetalleDTO.setProductoId(1L);
        mockDetalleDTO.setCantidad(2);

        mockVentaDTO = new VentaDTO();
        mockVentaDTO.setClienteId(1L);
        mockVentaDTO.setUsuarioId(1L);
        mockVentaDTO.setSerieComprobanteId(1L);
        mockVentaDTO.setDetalles(List.of(mockDetalleDTO));
    }

    // --- Pruebas para crearVenta ---
    @Test
    @DisplayName("Debe crear Venta a 'Contado' exitosamente")
    void testCrearVenta_Contado_Exitoso() {
        // Given
        mockVentaDTO.setFormaPagoId(1L);

        // Simular todas las búsquedas de la BD
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(mockCliente));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuario));
        when(formaPagoRepository.findById(1L)).thenReturn(Optional.of(mockFormaPagoContado));
        when(serieComprobanteRepository.findById(1L)).thenReturn(Optional.of(mockSerie));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(mockProducto1));

        // Simular el guardado en la BD
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Venta ventaGuardada = ventaService.crearVenta(mockVentaDTO);

        // Then
        assertThat(ventaGuardada).isNotNull();

        assertThat(ventaGuardada.getTotal()).isEqualTo(BigDecimal.valueOf(100.0).setScale(2, RoundingMode.HALF_UP));

        // Lógica de "Contado"
        assertThat(ventaGuardada.getDeuda()).isEqualTo(BigDecimal.ZERO);
        assertThat(ventaGuardada.getEstado()).isEqualTo(1); // 1 = Pagada

        // Efecto secundario: Stock del producto
        assertThat(mockProducto1.getStock()).isEqualTo(98); // 100 - 2

        // Efecto secundario: Correlativo de la serie
        assertThat(mockSerie.getCorrelativo_actual()).isEqualTo(100); // 99 + 1

        verify(ventaRepository, times(1)).save(any(Venta.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el Stock es insuficiente")
    void testCrearVenta_StockInsuficiente() {
        // Given
        mockVentaDTO.setFormaPagoId(1L); // Contado
        mockProducto1.setStock(1); // Solo hay 1 en stock
        mockDetalleDTO.setCantidad(2); // Pero se piden 2

        // Simular las búsquedas (solo las necesarias hasta el fallo)
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(mockCliente));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuario));
        when(formaPagoRepository.findById(1L)).thenReturn(Optional.of(mockFormaPagoContado));
        when(serieComprobanteRepository.findById(1L)).thenReturn(Optional.of(mockSerie));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(mockProducto1));

        // When
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ventaService.crearVenta(mockVentaDTO);
        });

        // Then
        assertTrue(exception.getMessage().contains("Stock insuficiente para el producto: " + mockProducto1.getNombre()));

        // El stock no debe cambiar
        assertThat(mockProducto1.getStock()).isEqualTo(1);
        // El correlativo no debe cambiar
        assertThat(mockSerie.getCorrelativo_actual()).isEqualTo(99);
        // La venta no debe guardarse
        verify(ventaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción si la Venta a 'Crédito' es inconsistente")
    void testCrearVenta_Credito_Inconsistente() {
        // Given
        mockVentaDTO.setFormaPagoId(2L); // Crédito
        mockVentaDTO.setMontoInicial(BigDecimal.valueOf(10.0)); // Total es 100.0

        // Creamos cuotas que suman 50.0
        CuotasProgramadasDTO cuotaDTO = new CuotasProgramadasDTO();
        cuotaDTO.setMonto(BigDecimal.valueOf(50.0));
        mockVentaDTO.setPlanDeCuotas(List.of(cuotaDTO));

        // Total Venta (100.0) != Monto Inicial (10.0) + Cuotas (50.0)

        // Simular las búsquedas
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(mockCliente));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuario));
        when(formaPagoRepository.findById(2L)).thenReturn(Optional.of(mockFormaPagoCredito));
        when(serieComprobanteRepository.findById(1L)).thenReturn(Optional.of(mockSerie));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(mockProducto1));

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ventaService.crearVenta(mockVentaDTO);
        });

        // Then
        assertTrue(exception.getMessage().contains("Error de consistencia"));
        verify(ventaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe crear Venta a 'Crédito' exitosamente")
    void testCrearVenta_Credito_Exitoso() {
        // Given
        mockVentaDTO.setFormaPagoId(2L); // Crédito
        mockVentaDTO.setMontoInicial(BigDecimal.valueOf(40.0)); // Total es 100.0

        CuotasProgramadasDTO cuotaDTO = new CuotasProgramadasDTO();
        cuotaDTO.setMonto(BigDecimal.valueOf(60.0)); // 40.0 + 60.0 = 100.0
        cuotaDTO.setFechaVencimiento(LocalDate.from(LocalDateTime.now().plusDays(30)));
        mockVentaDTO.setPlanDeCuotas(List.of(cuotaDTO));

        // Simular búsquedas
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(mockCliente));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuario));
        when(formaPagoRepository.findById(2L)).thenReturn(Optional.of(mockFormaPagoCredito));
        when(serieComprobanteRepository.findById(1L)).thenReturn(Optional.of(mockSerie));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(mockProducto1));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Venta ventaGuardada = ventaService.crearVenta(mockVentaDTO);

        // Then
        assertThat(ventaGuardada).isNotNull();

        // Lógica de "Crédito"
        // Deuda = Total (100.0) - Monto Inicial (40.0) = 60.0
        assertThat(ventaGuardada.getDeuda()).isEqualTo(BigDecimal.valueOf(60.0));
        assertThat(ventaGuardada.getEstado()).isEqualTo(0); // 0 = Pendiente

        // Verifica que se creó la cuota
        assertThat(ventaGuardada.getCuotas()).hasSize(1);
        assertThat(ventaGuardada.getCuotas().get(0).getMonto()).isEqualTo(BigDecimal.valueOf(60.0));

        // Verifica efectos secundarios
        assertThat(mockProducto1.getStock()).isEqualTo(98);
        assertThat(mockSerie.getCorrelativo_actual()).isEqualTo(100);
        verify(ventaRepository, times(1)).save(any(Venta.class));
    }


    // --- Pruebas para anularVenta ---

    @Test
    @DisplayName("Debe anular una Venta y restaurar el stock del producto")
    void testAnularVenta_Exitoso() {
        // Given
        // 1. Simula el producto con el stock que tiene DESPUÉS de la venta
        mockProducto1.setStock(98); // Stock actual (post-venta)

        // 2. Simula la venta que se va a anular (estado 1 = Pagada, o 0 = Pendiente)
        Venta ventaExistente = new Venta();
        ventaExistente.setEstado(1);

        DetalleVenta detalle = new DetalleVenta();
        detalle.setProducto(mockProducto1);
        detalle.setCantidad(2); // Se vendieron 2 unidades

        ventaExistente.getDetalleVentas().add(detalle);

        // 3. Configura los Mocks
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(ventaExistente));
        when(ventaRepository.save(any(Venta.class))).thenReturn(ventaExistente);

        // When
        ventaService.anularVenta(1L); // <-- Se llama UNA SOLA VEZ

        // Then
        // 1. Verifica que el estado de la venta cambió a 2 (Anulada)
        assertThat(ventaExistente.getEstado()).isEqualTo(2);

        // 2. Verifica que el stock se restauró
        // 98 (stock post-venta) + 2 (cantidad devuelta) = 100
        assertThat(mockProducto1.getStock()).isEqualTo(100);

        // 3. Verifica que se guardaron los cambios (una sola vez)
        verify(ventaRepository, times(1)).save(ventaExistente);
    }

    @Test
    @DisplayName("Debe lanzar excepción si se intenta anular una Venta ya anulada")
    void testAnularVenta_YaAnulada() {
        // Given
        Venta ventaAnulada = new Venta();
        ventaAnulada.setEstado(2); // 2 = Anulada

        when(ventaRepository.findById(1L)).thenReturn(Optional.of(ventaAnulada));

        // When
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            ventaService.anularVenta(1L);
        });

        // Then
        assertTrue(exception.getMessage().contains("ya se encuentra eliminada"));
        verify(ventaRepository, never()).save(any());
    }


    // --- Pruebas para registrarPago ---

    @Test
    @DisplayName("Debe registrar un pago parcial a una cuota")
    void testRegistrarPago_Parcial() {
        // Given
        Venta ventaCredito = new Venta();
        ventaCredito.setDeuda(BigDecimal.valueOf(100.0));
        ventaCredito.setEstado(0); // Pendiente

        Cuota cuota = new Cuota();
        cuota.setId(1L);
        cuota.setSaldo(BigDecimal.valueOf(100.0));
        cuota.setEstado(0); // Pendiente
        cuota.setVenta(ventaCredito); // Enlaza la cuota a la venta

        PagosDTO pagoDTO = new PagosDTO();
        pagoDTO.setCuotaId(1L);
        pagoDTO.setMontoPagado(BigDecimal.valueOf(40.0)); // Pago parcial

        when(cuotaRepository.findById(1L)).thenReturn(Optional.of(cuota));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Venta ventaActualizada = ventaService.registrarPago(pagoDTO);

        // Then
        // Verifica que se guardó el pago
        verify(pagoRepository, times(1)).save(any(Pago.class));

        // Verifica la cuota
        assertThat(cuota.getSaldo()).isEqualTo(BigDecimal.valueOf(60.0)); // 100 - 40
        assertThat(cuota.getEstado()).isEqualTo(0); // Sigue pendiente

        // Verifica la venta
        assertThat(ventaActualizada.getDeuda()).isEqualTo(BigDecimal.valueOf(60.0)); // 100 - 40
        assertThat(ventaActualizada.getEstado()).isEqualTo(0); // Sigue pendiente
    }

    @Test
    @DisplayName("Debe registrar un pago total y cambiar estados a 'Pagada'")
    void testRegistrarPago_Total() {
        // Given
        Venta ventaCredito = new Venta();
        ventaCredito.setDeuda(BigDecimal.valueOf(100.0));
        ventaCredito.setEstado(0);

        Cuota cuota = new Cuota();
        cuota.setId(1L);
        cuota.setSaldo(BigDecimal.valueOf(100.0));
        cuota.setEstado(0);
        cuota.setVenta(ventaCredito);

        PagosDTO pagoDTO = new PagosDTO();
        pagoDTO.setCuotaId(1L);
        pagoDTO.setMontoPagado(BigDecimal.valueOf(100.0)); // Pago total

        when(cuotaRepository.findById(1L)).thenReturn(Optional.of(cuota));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Venta ventaActualizada = ventaService.registrarPago(pagoDTO);

        // Then
        verify(pagoRepository, times(1)).save(any(Pago.class));

        // Verifica la cuota
        assertThat(cuota.getSaldo().compareTo(BigDecimal.ZERO)).isEqualTo(0);
        assertThat(cuota.getEstado()).isEqualTo(1); // 1 = Pagada

        // Verifica la venta
        assertThat(ventaActualizada.getDeuda().compareTo(BigDecimal.ZERO)).isEqualTo(0);
        assertThat(ventaActualizada.getEstado()).isEqualTo(1); // 1 = Pagada
    }

    @Test
    @DisplayName("Debe lanzar excepción si el monto a pagar es mayor que el saldo")
    void testRegistrarPago_MontoExcesivo() {
        // Given
        Cuota cuota = new Cuota();
        cuota.setId(1L);
        cuota.setSaldo(BigDecimal.valueOf(100.0));

        PagosDTO pagoDTO = new PagosDTO();
        pagoDTO.setCuotaId(1L);
        pagoDTO.setMontoPagado(BigDecimal.valueOf(101.0)); // Pago excesivo

        when(cuotaRepository.findById(1L)).thenReturn(Optional.of(cuota));

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ventaService.registrarPago(pagoDTO);
        });

        // Then
        assertTrue(exception.getMessage().contains("monto a pagar no puede ser mayor que el saldo"));
        verify(pagoRepository, never()).save(any());
    }
}
