package com.example.acceso.service.Interfaces;

public interface GenerarBoletaService {

    byte[] generarBoletaPdf(Long ventaId) throws Exception;

    String enviarBoletaPorCorreo(Long ventaId) throws Exception;

}
