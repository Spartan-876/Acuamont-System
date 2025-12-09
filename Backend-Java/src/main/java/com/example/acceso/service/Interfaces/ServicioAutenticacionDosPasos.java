package com.example.acceso.service.Interfaces;

public interface ServicioAutenticacionDosPasos {

    String generarNuevoSecreto();

    String generarUriDatosQr(String secreto, String email, String issuer);

    boolean esCodigoValido(String secreto, String codigo);

}
