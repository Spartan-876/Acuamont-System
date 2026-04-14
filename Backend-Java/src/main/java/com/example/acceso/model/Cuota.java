package com.example.acceso.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cuotas")
public class Cuota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_venta", nullable = false)
    @JsonBackReference("venta-cuotas")
    private Venta venta;

    @Column(nullable = false)
    private Integer numeroCuota;

    @Column(nullable = false)
    private BigDecimal monto;

    @Column(nullable = false)
    private BigDecimal saldo;

    @Column(nullable = false)
    private LocalDate fechaVencimiento;

    @Column(nullable = false)
    private Integer estado = 0;// 0 = pendiente, 1 = pagada

    public Cuota() {
    }

    public Cuota(Venta venta, Integer numeroCuota, BigDecimal monto, BigDecimal saldo, LocalDate fechaVencimiento) {
        this.venta = venta;
        this.numeroCuota = numeroCuota;
        this.monto = monto;
        this.saldo = saldo;
        this.fechaVencimiento = fechaVencimiento;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Venta getVenta() {
        return venta;
    }

    public void setVenta(Venta venta) {
        this.venta = venta;
    }

    public Integer getNumeroCuota() {
        return numeroCuota;
    }

    public void setNumeroCuota(Integer numeroCuota) {
        this.numeroCuota = numeroCuota;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public Integer getEstado() {
        return estado;
    }

    public void setEstado(Integer estado) {
        this.estado = estado;
    }
}
