package com.example.acceso.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public class VentaDTO {

    @NotNull(message = "El ID del cliente es obligatorio.")
    private Long clienteId;

    @NotNull(message = "El ID del usuario (vendedor) es obligatorio.")
    private Long usuarioId;

    @NotNull(message = "El ID de la serie del comprobante es obligatorio.")
    private Long serieComprobanteId;


    @NotNull(message = "El ID de la forma de pago es obligatorio.")
    private Long formaPagoId;

    @NotEmpty(message = "La venta debe contener al menos un producto.")
    @Valid
    private List<DetalleVentaDTO> detalles;

    private BigDecimal montoInicial;

    private List<CuotasProgramadasDTO> planDeCuotas;

    public VentaDTO() {
    }

    public VentaDTO(Long clienteId, Long usuarioId, Long serieComprobanteId, Long formaPagoId, List<DetalleVentaDTO> detalles, BigDecimal montoInicial, List<CuotasProgramadasDTO> planDeCuotas) {
        this.clienteId = clienteId;
        this.usuarioId = usuarioId;
        this.serieComprobanteId = serieComprobanteId;
        this.formaPagoId = formaPagoId;
        this.detalles = detalles;
        this.montoInicial = montoInicial;
        this.planDeCuotas = planDeCuotas;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getSerieComprobanteId() {
        return serieComprobanteId;
    }

    public void setSerieComprobanteId(Long serieComprobanteId) {
        this.serieComprobanteId = serieComprobanteId;
    }

    public Long getFormaPagoId() {
        return formaPagoId;
    }

    public void setFormaPagoId(Long formaPagoId) {
        this.formaPagoId = formaPagoId;
    }

    public List<DetalleVentaDTO> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleVentaDTO> detalles) {
        this.detalles = detalles;
    }

    public BigDecimal getMontoInicial() {
        return montoInicial;
    }

    public void setMontoInicial(BigDecimal montoInicial) {
        this.montoInicial = montoInicial;
    }

    public List<CuotasProgramadasDTO> getPlanDeCuotas() {
        return planDeCuotas;
    }

    public void setPlanDeCuotas(List<CuotasProgramadasDTO> planDeCuotas) {
        this.planDeCuotas = planDeCuotas;
    }
}
