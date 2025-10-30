package com.example.acceso.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import com.example.acceso.model.Cliente;
import com.example.acceso.repository.ClienteRepository;
import com.example.acceso.service.ClienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;


@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas del Servicio ClienteService")
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente clienteActivo;
    private Cliente clienteInactivo;

    @BeforeEach
    void setUp() {
        // Cliente de prueba principal
        clienteActivo = new Cliente();
        clienteActivo.setId(1L);
        clienteActivo.setNombre("Cliente Activo");
        clienteActivo.setDocumento("12345678");
        clienteActivo.setEstado(1); // 1 = Activo

        // Cliente de prueba para cambio de estado
        clienteInactivo = new Cliente();
        clienteInactivo.setId(2L);
        clienteInactivo.setNombre("Cliente Inactivo");
        clienteInactivo.setDocumento("87654321");
        clienteInactivo.setEstado(0); // 0 = Inactivo
    }

    // --- Pruebas para guardarCliente ---
    @Test
    @DisplayName("Debe guardar un cliente nuevo exitosamente")
    void testGuardarCliente_Exitoso() {
        // Given
        Cliente clienteNuevo = new Cliente();
        clienteNuevo.setNombre("Nuevo Cliente");
        clienteNuevo.setDocumento("11112222");


        when(clienteRepository.findByDocumento("11112222")).thenReturn(Optional.empty());

        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> {
            Cliente cli = invocation.getArgument(0);
            cli.setId(3L);
            return cli;
        });

        // When
        Cliente clienteGuardado = clienteService.guardarCliente(clienteNuevo);

        // Then
        assertThat(clienteGuardado).isNotNull();
        assertThat(clienteGuardado.getId()).isEqualTo(3L);
        assertThat(clienteGuardado.getNombre()).isEqualTo("Nuevo Cliente");

        verify(clienteRepository, times(1)).findByDocumento("11112222");
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Debe actualizar un cliente existente exitosamente")
    void testGuardarCliente_ActualizarExitoso() {
        // Given
        when(clienteRepository.findByDocumento(clienteActivo.getDocumento())).thenReturn(Optional.of(clienteActivo));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteActivo);

        // When
        clienteActivo.setNombre("Cliente Activo Actualizado");
        Cliente clienteGuardado = clienteService.guardarCliente(clienteActivo);

        // Then
        assertThat(clienteGuardado).isNotNull();
        assertThat(clienteGuardado.getNombre()).isEqualTo("Cliente Activo Actualizado");

        // Verifica que save fue llamado
        verify(clienteRepository, times(1)).findByDocumento(clienteActivo.getDocumento());
        verify(clienteRepository, times(1)).save(clienteActivo);
    }

    @Test
    @DisplayName("Debe lanzar excepción si el nombre es nulo")
    void testGuardarCliente_NombreNulo() {
        // Given
        Cliente clienteSinNombre = new Cliente();
        clienteSinNombre.setDocumento("12345678");

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            clienteService.guardarCliente(clienteSinNombre);
        });

        // Then
        assertTrue(exception.getMessage().contains("El nombre es obligatorio"));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción si el documento es nulo")
    void testGuardarCliente_DocumentoNulo() {
        // Given
        Cliente clienteSinDocumento = new Cliente();
        clienteSinDocumento.setNombre("Cliente de Prueba");

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            clienteService.guardarCliente(clienteSinDocumento);
        });

        // Then
        assertTrue(exception.getMessage().contains("El documento es obligatorio"));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción por documento duplicado al crear nuevo cliente")
    void testGuardarCliente_DocumentoDuplicado_NuevoCliente() {
        // Given
        when(clienteRepository.findByDocumento("12345678")).thenReturn(Optional.of(clienteActivo));

        Cliente clienteDuplicado = new Cliente();
        clienteDuplicado.setNombre("Cliente Duplicado");
        clienteDuplicado.setDocumento("12345678");

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            clienteService.guardarCliente(clienteDuplicado);
        });

        // Then
        assertTrue(exception.getMessage().contains("Ya existe un cliente con el mismo documento"));
        verify(clienteRepository, times(1)).findByDocumento("12345678");
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción por documento duplicado al actualizar a otro cliente")
    void testGuardarCliente_DocumentoDuplicado_ActualizarOtroCliente() {
        // Given
        when(clienteRepository.findByDocumento("87654321")).thenReturn(Optional.of(clienteInactivo));

        clienteActivo.setDocumento("87654321");

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            clienteService.guardarCliente(clienteActivo);
        });

        // Then
        assertTrue(exception.getMessage().contains("Ya existe un cliente con el mismo documento"));
        verify(clienteRepository, times(1)).findByDocumento("87654321");
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción por DataIntegrity (documento)")
    void testGuardarCliente_DataIntegrity_Documento() {
        // Given
        when(clienteRepository.findByDocumento(clienteActivo.getDocumento())).thenReturn(Optional.empty());
        when(clienteRepository.save(any(Cliente.class))).thenThrow(
                new DataIntegrityViolationException("Error: duplicate key (documento)")
        );

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            clienteService.guardarCliente(clienteActivo);
        });

        // Then
        assertTrue(exception.getMessage().contains("Ya existe un cliente con el mismo documento"));
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }


    // --- Pruebas para eliminarCliente ---
    @Test
    @DisplayName("Debe eliminar lógicamente un cliente (Estado 2)")
    void testEliminarCliente_Exitoso() {
        // Given
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteActivo));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteActivo);

        // When
        clienteService.eliminarCliente(1L);

        // Then
        assertThat(clienteActivo.getEstado()).isEqualTo(2);
        verify(clienteRepository, times(1)).findById(1L);
        verify(clienteRepository, times(1)).save(clienteActivo);
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar si el cliente no existe")
    void testEliminarCliente_NoEncontrado() {
        // Given
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            clienteService.eliminarCliente(99L);
        });

        // Then
        assertTrue(exception.getMessage().contains("Cliente no encontrado"));
        verify(clienteRepository, times(1)).findById(99L);
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar si el ID es inválido")
    void testEliminarCliente_IDInvalido() {
        // Prueba con ID nulo
        IllegalArgumentException exNulo = assertThrows(IllegalArgumentException.class, () -> {
            clienteService.eliminarCliente(null);
        });
        assertTrue(exNulo.getMessage().contains("ID de cliente inv"));

        // Prueba con ID 0
        IllegalArgumentException exCero = assertThrows(IllegalArgumentException.class, () -> {
            clienteService.eliminarCliente(0L);
        });
        assertTrue(exCero.getMessage().contains("ID de cliente inv"));

        verify(clienteRepository, never()).findById(any());
        verify(clienteRepository, never()).save(any());
    }


    // --- Pruebas para cambiarEstadoCliente ---
    @Test
    @DisplayName("Debe cambiar estado de Activo (1) a Inactivo (0)")
    void testCambiarEstadoCliente_DeActivoAInactivo() {
        // Given
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteActivo));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteActivo);

        // When
        Optional<Cliente> clienteActualizado = clienteService.cambiarEstadoCliente(1L);

        // Then
        assertThat(clienteActualizado).isPresent();
        assertThat(clienteActualizado.get().getEstado()).isEqualTo(0);
        verify(clienteRepository, times(1)).findById(1L);
        verify(clienteRepository, times(1)).save(clienteActivo);
    }

    @Test
    @DisplayName("Debe cambiar estado de Inactivo (0) a Activo (1)")
    void testCambiarEstadoCliente_DeInactivoAActivo() {
        // Given
        when(clienteRepository.findById(2L)).thenReturn(Optional.of(clienteInactivo));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteInactivo);

        // When
        Optional<Cliente> clienteActualizado = clienteService.cambiarEstadoCliente(2L);

        // Then
        assertThat(clienteActualizado).isPresent();
        assertThat(clienteActualizado.get().getEstado()).isEqualTo(1);
        verify(clienteRepository, times(1)).findById(2L);
        verify(clienteRepository, times(1)).save(clienteInactivo);
    }

    @Test
    @DisplayName("No debe cambiar estado si es Eliminado (2), pero debe guardar")
    void testCambiarEstadoCliente_EstadoEliminadoNoCambia() {
        // Given
        clienteActivo.setEstado(2);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteActivo));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteActivo);

        // When
        Optional<Cliente> clienteActualizado = clienteService.cambiarEstadoCliente(1L);

        // Then
        assertThat(clienteActualizado).isPresent();
        assertThat(clienteActualizado.get().getEstado()).isEqualTo(2);
        verify(clienteRepository, times(1)).findById(1L);
        verify(clienteRepository, times(1)).save(clienteActivo);
    }

    @Test
    @DisplayName("Debe lanzar excepción al cambiar estado si el ID es inválido")
    void testCambiarEstadoCliente_IDInvalido() {
        // Prueba con ID 0
        IllegalArgumentException exCero = assertThrows(IllegalArgumentException.class, () -> {
            clienteService.cambiarEstadoCliente(0L);
        });
        assertTrue(exCero.getMessage().contains("ID de cliente inv"));

        verify(clienteRepository, never()).findById(any());
        verify(clienteRepository, never()).save(any());
    }

    // --- Pruebas para métodos de listado y consulta ---
    @Test
    @DisplayName("Debe listar solo clientes no eliminados (Estado != 2)")
    void testListarClientes() {
        // Given
        when(clienteRepository.findAllByEstadoNot(2)).thenReturn(List.of(clienteActivo, clienteInactivo));

        // When
        List<Cliente> clientes = clienteService.listarClientes();

        // Then
        assertThat(clientes).isNotNull();
        assertThat(clientes).hasSize(2);
        verify(clienteRepository, times(1)).findAllByEstadoNot(2);
    }

    @Test
    @DisplayName("Debe retornar Optional.empty si el ID a obtener es inválido")
    void testObtenerClientePorId_IDInvalido() {
        // When
        Optional<Cliente> resNulo = clienteService.obtenerClientePorId(null);
        Optional<Cliente> resCero = clienteService.obtenerClientePorId(0L);

        // Then
        assertThat(resNulo).isEmpty();
        assertThat(resCero).isEmpty();
        verify(clienteRepository, never()).findById(any());
    }
}