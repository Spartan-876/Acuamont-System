package com.example.acceso.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class DetalleVentaDTO {

    @NotNull(message = "El ID del producto es obligatorio.")
    private Long productoId;

    @Min(value = 1, message = "La cantidad del producto debe ser como mínimo 1.")
    private int cantidad;

    public DetalleVentaDTO() {
    }

    public DetalleVentaDTO(Long productoId, int cantidad) {
        this.productoId = productoId;
        this.cantidad = cantidad;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
