package com.example.acceso.service;

import com.example.acceso.model.FormaPago;
import com.example.acceso.repository.FormaPagoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class FormaPagoService {

    private final FormaPagoRepository formaPagoRepository;

    public FormaPagoService(FormaPagoRepository formaPagoRepository) {
        this.formaPagoRepository = formaPagoRepository;
    }

    @Transactional(readOnly = true)
    public List<FormaPago> listarFormasPago() {
        return formaPagoRepository.findAllByEstadoNot(2);
    }

    @Transactional(readOnly = true)
    public Optional<FormaPago> obtenerFormaPagoPorId(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }

        return formaPagoRepository.findById(id);
    }

}
