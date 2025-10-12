package com.example.acceso.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ventas")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_SerieComprobante", nullable = false)
    private SerieComprobante serieComprobante;

    @NotNull
    @Column(nullable = false)
    private Integer correlativo;

    @ManyToOne
    @JoinColumn(name = "id_Cliente")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "id_Usuario")
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @NotNull
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @ManyToOne
    @JoinColumn(name = "id_FormaPago")
    private FormaPago formaPago;

    @NotNull
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal deuda = BigDecimal.ZERO;

    @OneToMany(
            mappedBy = "venta",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    private List<Cuota> cuotas = new ArrayList<>();

    @OneToMany(
            mappedBy = "venta",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    private List<DetalleVenta> detalleVentas = new ArrayList<>();

    @Column(nullable = false)
    private Integer estado = 1;

    public Venta() {}

    public Venta(SerieComprobante serieComprobante, Integer correlativo, Cliente cliente, Usuario usuario, LocalDateTime fecha, BigDecimal total, FormaPago formaPago, BigDecimal deuda, List<Cuota> cuotas, List<DetalleVenta> detalleVentas) {
        this.serieComprobante = serieComprobante;
        this.correlativo = correlativo;
        this.cliente = cliente;
        this.usuario = usuario;
        this.fecha = fecha;
        this.total = total;
        this.formaPago = formaPago;
        this.deuda = deuda;
        this.cuotas = cuotas;
        this.detalleVentas = detalleVentas;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SerieComprobante getSerieComprobante() {
        return serieComprobante;
    }

    public void setSerieComprobante(SerieComprobante serieComprobante) {
        this.serieComprobante = serieComprobante;
    }

    public Integer getCorrelativo() {
        return correlativo;
    }

    public void setCorrelativo(Integer correlativo) {
        this.correlativo = correlativo;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }


    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public FormaPago getFormaPago() {
        return formaPago;
    }

    public void setFormaPago(FormaPago formaPago) {
        this.formaPago = formaPago;
    }

    public BigDecimal getDeuda() {
        return deuda;
    }

    public void setDeuda(BigDecimal deuda) {
        this.deuda = deuda;
    }

    public List<Cuota> getCuotas() {
        return cuotas;
    }

    public void setCuotas(List<Cuota> cuotas) {
        this.cuotas = cuotas;
    }

    public List<DetalleVenta> getDetalleVentas() {
        return detalleVentas;
    }

    public void setDetalleVentas(List<DetalleVenta> detalleVentas) {
        this.detalleVentas = detalleVentas;
    }

    public Integer getEstado() {
        return estado;
    }

    public void setEstado(Integer estado) {
        this.estado = estado;
    }
}
