package com.example.acceso.service.Interfaces;

import com.example.acceso.model.FormaPago;

import java.util.List;
import java.util.Optional;

public interface FormaPagoService {

    List<FormaPago> listarFormasPago();

    Optional<FormaPago> obtenerFormaPagoPorId(Long id);

}
