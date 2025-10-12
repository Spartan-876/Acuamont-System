package com.example.acceso.service;

import com.example.acceso.model.Cliente;
import com.example.acceso.model.Venta;
import com.example.acceso.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final SerieComprobanteRepository serieComprobanteRepository;
    private final CuotaRepository cuotaRepository;
    private final PagoRepository pagoRepository;
    private final FormaPagoRepository formaPagoRepository;
    private final ProductoRepository productoRepository;
    private final DetalleVentaRepository detalleVentaRepository;

    public VentaService(VentaRepository ventaRepository, ClienteRepository clienteRepository, UsuarioRepository usuarioRepository, SerieComprobanteRepository serieComprobanteRepository, CuotaRepository cuotaRepository, PagoRepository pagoRepository, FormaPagoRepository formaPagoRepository, ProductoRepository productoRepository, DetalleVentaRepository detalleVentaRepository) {
        this.ventaRepository = ventaRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.serieComprobanteRepository = serieComprobanteRepository;
        this.cuotaRepository = cuotaRepository;
        this.pagoRepository = pagoRepository;
        this.formaPagoRepository = formaPagoRepository;
        this.productoRepository = productoRepository;
        this.detalleVentaRepository = detalleVentaRepository;
    }

    @Transactional
    public List<Venta> listarVentas() {
        return ventaRepository.findAllByEstadoNot(2);
    }

   

}
