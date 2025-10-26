package com.example.acceso.repository;

import com.example.acceso.model.TipoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TipoMovimeintoRepository extends JpaRepository<TipoMovimiento, Long> {

    List<TipoMovimiento> findAllByEstadoNot(Integer estado);

}
