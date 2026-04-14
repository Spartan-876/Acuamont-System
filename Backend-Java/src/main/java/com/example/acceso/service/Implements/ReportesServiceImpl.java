package com.example.acceso.service.Implements;

import com.example.acceso.DTO.ReporteUtilidadProductoDTO;
import com.example.acceso.DTO.ReporteUtilidadVentaDTO;
import com.example.acceso.DTO.ReporteUttilidadUsuarioDTO;
import com.example.acceso.repository.ReportesRepository;
import com.example.acceso.service.Interfaces.ReportesService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportesServiceImpl implements ReportesService {

    private final ReportesRepository reportesRepository;

    public ReportesServiceImpl(ReportesRepository reportesRepository) {
        this.reportesRepository = reportesRepository;
    }

    @Override
    public List<ReporteUtilidadVentaDTO> obtenerUtilidadPorVenta() {
        return reportesRepository.obtenerUtilidadPorVenta();
    }

    @Override
    public List<ReporteUtilidadVentaDTO> obtenerUtilidadPorVentaRango(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio != null && fin != null && fin.isBefore(inicio)) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la de inicio");
        }
        return reportesRepository.obtenerUtilidadPorVentaRango(inicio, fin);
    }

    @Override
    public List<ReporteUttilidadUsuarioDTO> obtenerUtilidadPorUsuario() {
        return reportesRepository.obtenerUtilidadPorUsuario();
    }

    @Override
    public List<ReporteUttilidadUsuarioDTO> obtenerUtilidadPorUsuarioRango(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio != null && fin != null && fin.isBefore(inicio)) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la de inicio");
        }
        return reportesRepository.obtenerUtilidadPorUsuarioRango(inicio,fin);
    }

    @Override
    public List<ReporteUtilidadProductoDTO> obtenerUtilidadPorProducto() {
        return reportesRepository.obtenerUtilidadPorProducto();    }

    @Override
    public List<ReporteUtilidadProductoDTO> obtenerUtilidadPorProductoRango(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio != null && fin != null && fin.isBefore(inicio)) {
            throw new IllegalArgumentException("Rango de fechas inválido");
        }
        return reportesRepository.obtenerUtilidadPorProductoRango(inicio, fin);
    }
}
