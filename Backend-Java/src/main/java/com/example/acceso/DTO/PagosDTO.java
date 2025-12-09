package com.example.acceso.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class PagosDTO {

    @NotNull
    private Long cuotaId;

    @NotNull
    @Positive
    private BigDecimal montoPagado;

    @Size(max = 250, message = "El comentario debe tener máximo 250 caracteres")
    private String comentario;

    @NotBlank(message = "Debe seleccionar un método de pago.")
    private String metodoPago;

    public PagosDTO() {
    }

    public PagosDTO(Long cuotaId, BigDecimal montoPagado, String comentario, String metodoPago) {
        this.cuotaId = cuotaId;
        this.montoPagado = montoPagado;
        this.comentario = comentario;
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

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }
}
