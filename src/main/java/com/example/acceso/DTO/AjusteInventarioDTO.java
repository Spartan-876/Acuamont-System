package com.example.acceso.DTO;

import java.time.LocalDate;

public class AjusteInventarioDTO {

    private Long productoId;
    private Long tipoMovimientoId;
    private Integer cantidad;
    private String comentario;

    public AjusteInventarioDTO() {
    }

    public AjusteInventarioDTO(Long productoId, Long tipoMovimientoId, Integer cantidad, String comentario) {
        this.productoId = productoId;
        this.tipoMovimientoId = tipoMovimientoId;
        this.cantidad = cantidad;
        this.comentario = comentario != null ? comentario : "";
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public Long getTipoMovimientoId() {
        return tipoMovimientoId;
    }

    public void setTipoMovimientoId(Long tipoMovimientoId) {
        this.tipoMovimientoId = tipoMovimientoId;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}
