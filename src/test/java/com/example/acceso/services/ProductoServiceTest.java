package com.example.acceso.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import com.example.acceso.model.Producto;
import com.example.acceso.repository.ProductoRepository;
import com.example.acceso.service.Implements.ProductoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas del Servicio ProductoService")
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoServiceImpl productoService;

    // JUnit 5 proveerá una carpeta temporal para las pruebas de archivos
    @TempDir
    Path tempDir;

    private Producto producto;
    private MockMultipartFile mockFotoFile;

    @BeforeEach
    void setUp() {

        ReflectionTestUtils.setField(productoService, "uploadDir", tempDir.toString() + "/");

        // Producto de prueba base
        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Producto Prueba");
        producto.setDescripcion("Descripcion Prueba");
        producto.setPrecioCompra(10.0);
        producto.setPrecioVenta(20.0);
        producto.setStock(100);
        producto.setStockSeguridad(10);
        producto.setEstado(1);

        // Archivo de imagen simulado
        mockFotoFile = new MockMultipartFile(
                "fotoFile",
                "test-image.jpg",
                "image/jpeg",
                "some-image-bytes".getBytes()
        );
    }

    // --- Pruebas para guardarProducto (Validaciones) ---

    @Test
    @DisplayName("Debe lanzar excepción si el nombre es nulo")
    void testGuardarProducto_NombreNulo() {
        producto.setNombre(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            productoService.guardarProducto(producto, null);
        });

        assertTrue(ex.getMessage().contains("El nombre es obligatorio"));
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción si la descripción es nula")
    void testGuardarProducto_DescripcionNula() {
        producto.setDescripcion(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            productoService.guardarProducto(producto, null);
        });

        assertTrue(ex.getMessage().contains("La descripci"));
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción si el precio de compra es nulo")
    void testGuardarProducto_PrecioCompraNulo() {
        producto.setPrecioCompra(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            productoService.guardarProducto(producto, null);
        });

        assertTrue(ex.getMessage().contains("precio de compra es obligatorio"));
        verify(productoRepository, never()).save(any());
    }


    // --- Pruebas para guardarProducto (Lógica de Archivos y BD) ---
    @Test
    @DisplayName("Debe guardar un producto nuevo CON imagen")
    void testGuardarProducto_ExitosoConImagen() throws IOException {
        // Given
        producto.setId(null);
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> {
            Producto p = inv.getArgument(0);
            p.setId(2L);
            return p;
        });

        // When
        Producto guardado = productoService.guardarProducto(producto, mockFotoFile);

        // Then
        assertThat(guardado).isNotNull();
        assertThat(guardado.getId()).isEqualTo(2L);
        assertThat(guardado.getImagen()).isNotNull();
        assertThat(guardado.getImagen()).endsWith("_test-image.jpg");


        Path rutaGuardada = Paths.get(tempDir.toString() + "/" + guardado.getImagen());
        assertThat(Files.exists(rutaGuardada)).isTrue();

        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    @DisplayName("Debe actualizar un producto y reemplazar la imagen anterior")
    void testGuardarProducto_ActualizarReemplazandoImagen() throws IOException {
        // Given
        String nombreImagenVieja = "imagen-vieja.jpg";
        Path rutaImagenVieja = Paths.get(tempDir.toString() + "/" + nombreImagenVieja);
        Files.write(rutaImagenVieja, "bytes-viejos".getBytes());
        assertTrue(Files.exists(rutaImagenVieja));

        producto.setImagen(nombreImagenVieja);

        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        // When
        Producto actualizado = productoService.guardarProducto(producto, mockFotoFile);

        // Then
        assertThat(actualizado.getImagen()).isNotNull();
        assertThat(actualizado.getImagen()).endsWith("_test-image.jpg");

        assertThat(Files.exists(rutaImagenVieja)).isFalse();

        Path rutaImagenNueva = Paths.get(tempDir.toString() + "/" + actualizado.getImagen());
        assertThat(Files.exists(rutaImagenNueva)).isTrue();

        verify(productoRepository, times(1)).save(producto);
    }

    @Test
    @DisplayName("Debe guardar un producto sin imagen (null file)")
    void testGuardarProducto_ExitosoSinImagen() {
        // Given
        producto.setId(null);
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        // When
        Producto guardado = productoService.guardarProducto(producto, null);

        // Then
        assertThat(guardado).isNotNull();
        assertThat(guardado.getImagen()).isNull();
        verify(productoRepository, times(1)).save(producto);
    }

    @Test
    @DisplayName("Debe lanzar excepción por nombre duplicado (DataIntegrity)")
    void testGuardarProducto_NombreDuplicado() {
        // Given
        when(productoRepository.save(any(Producto.class))).thenThrow(
                new DataIntegrityViolationException("Error: duplicate key (nombre)")
        );

        // When
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            productoService.guardarProducto(producto, null);
        });

        // Then
        assertTrue(ex.getMessage().contains("Ya existe un producto con el mismo nombre"));
    }

    // --- Pruebas para eliminarProducto ---
    @Test
    @DisplayName("Debe eliminar lógicamente el producto y físicamente su imagen")
    void testEliminarProducto_Exitoso() throws IOException {
        // Given
        String nombreImagen = "imagen-a-borrar.jpg";
        Path rutaImagen = Paths.get(tempDir.toString() + "/" + nombreImagen);
        Files.write(rutaImagen, "bytes".getBytes());
        assertTrue(Files.exists(rutaImagen));

        producto.setImagen(nombreImagen);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        // When
        productoService.eliminarProducto(1L);

        // Then
        assertThat(producto.getEstado()).isEqualTo(2);
        assertThat(producto.getImagen()).isNull();
        verify(productoRepository, times(1)).findById(1L);
        verify(productoRepository, times(1)).save(producto);

        assertThat(Files.exists(rutaImagen)).isFalse();
    }

    @Test
    @DisplayName("Debe eliminar producto que no tiene imagen (sin error)")
    void testEliminarProducto_SinImagen() {
        // Given
        producto.setImagen(null); // El producto no tiene imagen
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        // When
        productoService.eliminarProducto(1L);

        // Then
        assertThat(producto.getEstado()).isEqualTo(2);
        assertThat(producto.getImagen()).isNull();
        verify(productoRepository, times(1)).findById(1L);
        verify(productoRepository, times(1)).save(producto);
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar si el ID es inválido")
    void testEliminarProducto_IDInvalido() {
        // When
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            productoService.eliminarProducto(0L);
        });

        // Then
        assertTrue(ex.getMessage().contains("producto inv"));
        verify(productoRepository, never()).findById(any());
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar si el producto no existe")
    void testEliminarProducto_NoEncontrado() {
        // Given
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            productoService.eliminarProducto(99L);
        });

        // Then
        assertTrue(ex.getMessage().contains("Producto no encontrado"));
        verify(productoRepository, times(1)).findById(99L);
        verify(productoRepository, never()).save(any());
    }

    // --- Pruebas para cambiarEstadoProducto ---
    @Test
    @DisplayName("Debe cambiar estado de Activo (1) a Inactivo (0)")
    void testCambiarEstadoProducto_DeActivoAInactivo() {
        // Given
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        // When
        Optional<Producto> actualizado = productoService.cambiarEstadoProducto(1L);

        // Then
        assertThat(actualizado).isPresent();
        assertThat(actualizado.get().getEstado()).isEqualTo(0);
        verify(productoRepository, times(1)).save(producto);
    }
}