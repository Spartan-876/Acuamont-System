package com.example.acceso.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ReporteUtilidadVentaDTO {

    String getDocumento();
    String getCliente();
    LocalDateTime getFecha();
    BigDecimal getTotalVenta();
    BigDecimal getUtilidad();

}
