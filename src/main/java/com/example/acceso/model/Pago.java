package com.example.acceso.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_cuota", nullable = false)
    private Cuota cuota;

    @Column(nullable = false)
    private BigDecimal montoPagado;

    @Column(nullable = false)
    private LocalDateTime fechaPago;

    @Column(nullable = false, length = 50)
    private String metodoPago;

    @Column(nullable = false)
    private Integer estado = 1; // 1 = Activo, 0 = Inactivo

    public Pago() {}

    public Pago(Cuota cuota, BigDecimal montoPagado, LocalDateTime fechaPago, String metodoPago) {
        this.cuota = cuota;
        this.montoPagado = montoPagado;
        this.fechaPago = fechaPago;
        this.metodoPago = metodoPago;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cuota getCuota() {
        return cuota;
    }

    public void setCuota(Cuota cuota) {
        this.cuota = cuota;
    }

    public BigDecimal getMontoPagado() {
        return montoPagado;
    }

    public void setMontoPagado(BigDecimal montoPagado) {
        this.montoPagado = montoPagado;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public Integer getEstado() {
        return estado;
    }

    public void setEstado(Integer estado) {
        this.estado = estado;
    }
}
