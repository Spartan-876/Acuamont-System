package com.example.acceso.DTO;

import java.math.BigDecimal;

public interface ReporteUtilidadProductoDTO {
    String getProducto();
    Integer getCantidadVendida();
    BigDecimal getTotalVenta();
    BigDecimal getUtilidad();
}
