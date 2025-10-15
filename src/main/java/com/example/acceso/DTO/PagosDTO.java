package com.example.acceso.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public class PagosDTO {

    @NotNull
    private Long cuotaId;

    @NotNull
    @Positive
    private BigDecimal montoPagado;

    @NotBlank(message = "Debe seleccionar un método de pago.")
    private String metodoPago;

    public PagosDTO() {
    }

    public PagosDTO(Long cuotaId, BigDecimal montoPagado, String metodoPago) {
        this.cuotaId = cuotaId;
        this.montoPagado = montoPagado;
        this.metodoPago = metodoPago;
    }

    public Long getCuotaId() {
        return cuotaId;
    }

    public void setCuotaId(Long cuotaId) {
        this.cuotaId = cuotaId;
    }

    public BigDecimal getMontoPagado() {
        return montoPagado;
    }

    public void setMontoPagado(BigDecimal montoPagado) {
        this.montoPagado = montoPagado;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }
}
