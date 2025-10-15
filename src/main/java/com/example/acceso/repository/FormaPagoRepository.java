package com.example.acceso.repository;

import com.example.acceso.model.Categoria;
import com.example.acceso.model.FormaPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormaPagoRepository extends JpaRepository<FormaPago, Long> {

    List<FormaPago> findAllByEstadoNot(Integer estado);

}
