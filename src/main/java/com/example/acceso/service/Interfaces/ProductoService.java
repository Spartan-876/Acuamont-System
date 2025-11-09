package com.example.acceso.service.Interfaces;

import com.example.acceso.model.Producto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface ProductoService {

    List<Producto> listarProductos();

    Producto guardarProducto(Producto producto, MultipartFile fotoFile);

    long contarProductos();

    Optional<Producto> obtenerProductoPorId(Long id);

    Optional<Producto> obtenerProductoPorNombre(String nombre);

    void eliminarProducto(Long id);

    Optional<Producto> cambiarEstadoProducto(Long id);


}
