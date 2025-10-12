package com.example.acceso.repository;

import com.example.acceso.model.FormaPago;
import jakarta.persistence.Entity;
import org.springframework.data.jpa.repository.JpaRepository;

@Entity
public interface FormaPagoRepository extends JpaRepository<FormaPago, Integer> {
}
