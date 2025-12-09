package com.example.acceso.DTO;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CuotasProgramadasDTO {

    @NotNull(message = "El monto de la cuota no puede ser nulo.")
    private BigDecimal monto;

    @NotNull(message = "La fecha de vencimiento de la cuota no puede ser nula.")
    private LocalDate fechaVencimiento;

    public CuotasProgramadasDTO() {
    }

    public CuotasProgramadasDTO(BigDecimal monto, LocalDate fechaVencimiento) {
        this.monto = monto;
        this.fechaVencimiento = fechaVencimiento;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }
}
