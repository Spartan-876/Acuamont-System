package com.example.acceso.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import com.example.acceso.model.Categoria;
import com.example.acceso.repository.CategoriaRepository;
import com.example.acceso.service.CategoriaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;


@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas del Servicio CategoriaService")
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    private Categoria categoriaActiva;
    private Categoria categoriaInactiva;

    @BeforeEach
    void setUp() {
        categoriaActiva = new Categoria();
        categoriaActiva.setId(1L);
        categoriaActiva.setNombre("Bebidas");
        categoriaActiva.setEstado(1); // 1 = Activo

        categoriaInactiva = new Categoria();
        categoriaInactiva.setId(2L);
        categoriaInactiva.setNombre("Congelados");
        categoriaInactiva.setEstado(0); // 0 = Inactivo
    }

    // --- Pruebas para guardarCategoria ---
    @Test
    @DisplayName("Debe guardar y retornar una categoría válida")
    void testGuardarCategoria_Exitoso() {
        Categoria categoriaNueva = new Categoria();
        categoriaNueva.setNombre("  Frutas  ");

        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> {
            Categoria cat = invocation.getArgument(0);
            cat.setId(3L);
            return cat;
        });

        // When: Ejecutamos el método
        Categoria categoriaGuardada = categoriaService.guardarCategoria(categoriaNueva);

        // Then: Verificamos el resultado
        assertThat(categoriaGuardada).isNotNull();
        assertThat(categoriaGuardada.getId()).isEqualTo(3L);
        assertThat(categoriaGuardada.getNombre()).isEqualTo("Frutas");

        // Verifica que save() fue llamado 1 vez
        verify(categoriaRepository, times(1)).save(any(Categoria.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el nombre es nulo")
    void testGuardarCategoria_NombreNulo() {
        Categoria categoriaNula = new Categoria();
        categoriaNula.setNombre(null);

        // When & Then
        assertThatThrownBy(() -> {
            categoriaService.guardarCategoria(categoriaNula);
        })
                .isInstanceOf(IllegalArgumentException.class)
                // 👇 CORREGIDO: Usamos hasMessageContaining
                .hasMessageContaining("El nombre es obligatorio");

        // Verifica que save() NUNCA fue llamado
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción si el nombre está vacío (solo espacios)")
    void testGuardarCategoria_NombreVacio() {
        Categoria categoriaVacia = new Categoria();
        categoriaVacia.setNombre("   ");

        // When & Then
        assertThatThrownBy(() -> {
            categoriaService.guardarCategoria(categoriaVacia);
        })
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El nombre es obligatorio");

        // Verifica que save() NUNCA fue llamado
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción por nombre duplicado (DataIntegrity)")
    void testGuardarCategoria_NombreDuplicado() {
        when(categoriaRepository.save(any(Categoria.class))).thenThrow(
                new DataIntegrityViolationException("Error: duplicate key value (nombre)")
        );

        // When & Then
        assertThatThrownBy(() -> {
            categoriaService.guardarCategoria(categoriaActiva);
        })
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Ya existe una categoría con el mismo nombre");

        verify(categoriaRepository, times(1)).save(any(Categoria.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción genérica por otra DataIntegrity")
    void testGuardarCategoria_OtraDataIntegrityError() {
        when(categoriaRepository.save(any(Categoria.class))).thenThrow(
                new DataIntegrityViolationException("Error: foreign key constraint failed")
        );

        // When & Then
        assertThatThrownBy(() -> {
            categoriaService.guardarCategoria(categoriaActiva);
        })
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Error de integridad de datos");

        verify(categoriaRepository, times(1)).save(any(Categoria.class));
    }

    // --- Pruebas para eliminarCategoria ---
    @Test
    @DisplayName("Debe eliminar lógicamente una categoría (Estado 2)")
    void testEliminarCategoria_Exitoso() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaActiva));
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoriaActiva);

        // When
        categoriaService.eliminarCategoria(1L);

        // Then
        assertThat(categoriaActiva.getEstado()).isEqualTo(2); // Verifica el cambio de estado
        verify(categoriaRepository, times(1)).findById(1L);
        verify(categoriaRepository, times(1)).save(categoriaActiva); // Verifica que se guardó el cambio
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar si la categoría no existe")
    void testEliminarCategoria_NoEncontrada() {

        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            categoriaService.eliminarCategoria(99L);
        });

        // Then
        String message = exception.getMessage();
        assertTrue(message.contains("Categor") && message.contains("no encontrada"));

        // Verificamos las llamadas al mock
        verify(categoriaRepository, times(1)).findById(99L);
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar si el ID es nulo o <= 0")
    void testEliminarCategoria_IDInvalido() {

        // --- Prueba con ID nulo ---
        IllegalArgumentException exceptionNulo = assertThrows(IllegalArgumentException.class, () -> {
            categoriaService.eliminarCategoria(null);
        });

        String messageNulo = exceptionNulo.getMessage();
        assertTrue(messageNulo.contains("ID de categor") && messageNulo.contains("inv"));


        // --- Prueba con ID 0 ---
        IllegalArgumentException exceptionCero = assertThrows(IllegalArgumentException.class, () -> {
            categoriaService.eliminarCategoria(0L);
        });

        String messageCero = exceptionCero.getMessage();
        assertTrue(messageCero.contains("ID de categor") && messageCero.contains("inv"));

        // Verificamos que el repositorio nunca fue contactado
        verify(categoriaRepository, never()).findById(any());
        verify(categoriaRepository, never()).save(any());
    }

    // --- Pruebas para cambiarEstadoCategoria ---
    @Test
    @DisplayName("Debe cambiar estado de Activo (1) a Inactivo (0)")
    void testCambiarEstadoCategoria_DeActivoAInactivo() {

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaActiva));
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoriaActiva);

        // When
        Optional<Categoria> categoriaActualizada = categoriaService.cambiarEstadoCategoria(1L);

        // Then
        assertThat(categoriaActualizada).isPresent();
        assertThat(categoriaActualizada.get().getEstado()).isEqualTo(0);
        verify(categoriaRepository, times(1)).findById(1L);
        verify(categoriaRepository, times(1)).save(categoriaActiva);
    }

    @Test
    @DisplayName("Debe cambiar estado de Inactivo (0) a Activo (1)")
    void testCambiarEstadoCategoria_DeInactivoAActivo() {

        when(categoriaRepository.findById(2L)).thenReturn(Optional.of(categoriaInactiva));
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoriaInactiva);

        // When
        Optional<Categoria> categoriaActualizada = categoriaService.cambiarEstadoCategoria(2L);

        // Then
        assertThat(categoriaActualizada).isPresent();
        assertThat(categoriaActualizada.get().getEstado()).isEqualTo(1);
        verify(categoriaRepository, times(1)).findById(2L);
        verify(categoriaRepository, times(1)).save(categoriaInactiva);
    }

    @Test
    @DisplayName("No debe cambiar estado si es Eliminado (2)")
    void testCambiarEstadoCategoria_EstadoEliminadoNoCambia() {
        // Given
        categoriaActiva.setEstado(2);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaActiva));
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoriaActiva);

        // When
        Optional<Categoria> categoriaActualizada = categoriaService.cambiarEstadoCategoria(1L);

        // Then
        assertThat(categoriaActualizada).isPresent();
        assertThat(categoriaActualizada.get().getEstado()).isEqualTo(2);
        verify(categoriaRepository, times(1)).findById(1L);
        verify(categoriaRepository, times(1)).save(categoriaActiva);
    }

    @Test
    @DisplayName("Debe retornar Optional.empty si la categoría a cambiar estado no existe")
    void testCambiarEstadoCategoria_NoEncontrada() {

        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        Optional<Categoria> resultado = categoriaService.cambiarEstadoCategoria(99L);

        // Then
        assertThat(resultado).isEmpty();
        verify(categoriaRepository, times(1)).findById(99L);
        verify(categoriaRepository, never()).save(any());
    }

    // --- Pruebas para métodos de listado y consulta ---
    @Test
    @DisplayName("Debe listar solo categorías no eliminadas (Estado != 2)")
    void testListarCategorias() {

        when(categoriaRepository.findAllByEstadoNot(2)).thenReturn(List.of(categoriaActiva, categoriaInactiva));

        // When
        List<Categoria> categorias = categoriaService.listarCategorias();

        // Then
        assertThat(categorias).isNotNull();
        assertThat(categorias).hasSize(2);
        assertThat(categorias).containsExactly(categoriaActiva, categoriaInactiva);
        verify(categoriaRepository, times(1)).findAllByEstadoNot(2);
    }

    @Test
    @DisplayName("Debe retornar Optional.empty si el ID a obtener es nulo o <= 0")
    void testObtenerCategoriaPorId_IDInvalido() {
        // When
        Optional<Categoria> resNulo = categoriaService.obtenerCategoriaPorId(null);
        Optional<Categoria> resCero = categoriaService.obtenerCategoriaPorId(0L);

        // Then
        assertThat(resNulo).isEmpty();
        assertThat(resCero).isEmpty();
        verify(categoriaRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Debe verificar si existe categoría con trim y lowercase")
    void testExisteCategoria_Exitoso() {

        when(categoriaRepository.existsByNombre("verduras")).thenReturn(true);

        // When
        boolean existe = categoriaService.existeCategoria("  Verduras  ");

        // Then
        assertThat(existe).isTrue();
        verify(categoriaRepository, times(1)).existsByNombre("verduras");
    }

    @Test
    @DisplayName("Debe retornar false si el nombre a verificar es nulo o vacío")
    void testExisteCategoria_NombreInvalido() {
        // When
        boolean existeNulo = categoriaService.existeCategoria(null);
        boolean existeVacio = categoriaService.existeCategoria("   ");

        // Then
        assertThat(existeNulo).isFalse();
        assertThat(existeVacio).isFalse();
        verify(categoriaRepository, never()).existsByNombre(any());
    }
}