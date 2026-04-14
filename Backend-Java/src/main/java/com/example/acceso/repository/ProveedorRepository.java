package com.example.acceso.repository;

import com.example.acceso.model.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    Optional<Proveedor> findByDocumento(String documento);

    boolean existsByDocumento(String documento);

    List<Proveedor> findAllByEstadoNot(Integer estado);

    Long countByEstadoNot(Integer estado);

}
