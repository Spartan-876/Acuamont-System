package com.example.acceso.repository;

import com.example.acceso.model.RedSocial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de Spring Data JPA para la entidad {@link RedSocial}.
 *
 * <p>Proporciona métodos para realizar operaciones CRUD (Crear, Leer, Actualizar, Eliminar)
 * en la base de datos para las redes sociales, así como consultas personalizadas derivadas
 * de los nombres de los métodos.</p>
 */
@Repository
public interface RedSocialRepository  extends JpaRepository<RedSocial, Long> {

    /**
     * Obtiene una lista de todas las redes sociales cuyo estado no coincide con el valor proporcionado.
     * <p>Se utiliza comúnmente para excluir las redes sociales eliminadas lógicamente (estado = 2).</p>
     *
     * @param estado El estado a excluir.
     * @return Una lista de objetos {@link RedSocial}.
     */
    List<RedSocial> findAllByEstadoNot(Integer estado);

    /**
     * Obtiene una lista de todas las redes sociales que tienen un estado específico.
     * <p>Se utiliza para encontrar redes sociales activas (estado = 1) o inactivas (estado = 0).</p>
     *
     * @param estado El estado a buscar.
     * @return Una lista de objetos {@link RedSocial}.
     */
    List<RedSocial> findAllByEstado(Integer estado);
}
