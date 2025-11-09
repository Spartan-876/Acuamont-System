package com.example.acceso.service.Interfaces;

import com.example.acceso.model.Cliente;

import java.util.List;
import java.util.Optional;

public interface ClienteService {

    List<Cliente> listarClientes();

    Cliente guardarCliente(Cliente cliente);

    long contarClientes();

    Optional<Cliente> obtenerClientePorId(Long id);

    Optional<Cliente> obtenerClientePorDocumento(String documento);

    void eliminarCliente(Long id);

    Optional<Cliente> cambiarEstadoCliente(Long id);

}
