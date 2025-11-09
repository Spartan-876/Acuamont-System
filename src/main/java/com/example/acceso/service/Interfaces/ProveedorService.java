package com.example.acceso.service.Interfaces;

import com.example.acceso.model.Proveedor;

import java.util.List;
import java.util.Optional;

public interface ProveedorService {

    List<Proveedor> listarProveedores();

    Proveedor guardarProveedor(Proveedor proveedor);

    long contarProveedores();

    Optional<Proveedor> obtenerProveedorPorId(Long id);

    Optional<Proveedor> obtenerProveedorPorDocumento(String documento);

    void eliminarProveedor(Long id);

    Optional<Proveedor> cambiarEstadoProveedor(Long id);

}
