package com.example.acceso.repository;

import com.example.acceso.model.FormaPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de Spring Data JPA para la entidad {@link FormaPago}.
 *
 * <p>Proporciona métodos para realizar operaciones CRUD (Crear, Leer, Actualizar, Eliminar)
 * en la base de datos para las formas de pago, así como consultas personalizadas derivadas
 * de los nombres de los métodos.</p>
 */
@Repository
public interface FormaPagoRepository extends JpaRepository<FormaPago, Long> {

    /**
     * Obtiene una lista de todas las formas de pago cuyo estado no coincide con el valor proporcionado.
     * <p>Se utiliza comúnmente para excluir las formas de pago eliminadas lógicamente (estado = 2).</p>
     *
     * @param estado El estado a excluir.
     * @return Una lista de objetos {@link FormaPago}.
     */
    List<FormaPago> findAllByEstadoNot(Integer estado);

}
