package com.example.acceso.service.Interfaces;

import com.example.acceso.DTO.ReporteUtilidadProductoDTO;
import com.example.acceso.DTO.ReporteUtilidadVentaDTO;
import com.example.acceso.DTO.ReporteUttilidadUsuarioDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportesService {

    List<ReporteUtilidadVentaDTO> obtenerUtilidadPorVenta();

    List<ReporteUtilidadVentaDTO> obtenerUtilidadPorVentaRango(LocalDateTime inicio, LocalDateTime fin);

    List<ReporteUttilidadUsuarioDTO> obtenerUtilidadPorUsuario();

    List<ReporteUttilidadUsuarioDTO> obtenerUtilidadPorUsuarioRango(LocalDateTime inicio, LocalDateTime fin);

    List<ReporteUtilidadProductoDTO> obtenerUtilidadPorProducto();

    List<ReporteUtilidadProductoDTO> obtenerUtilidadPorProductoRango(LocalDateTime inicio, LocalDateTime fin);

}
