// Define el paquete al que pertenece la clase.
package com.example.acceso.controller;

// Importaciones para el manejo de logs y excepciones específicas.
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Manejador de excepciones global para toda la aplicación.
 *
 * Esta clase utiliza {@link ControllerAdvice} para interceptar y procesar
 * excepciones que ocurren en cualquier controlador, proporcionando un punto
 * centralizado para la gestión de errores.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja las excepciones {@link TypeMismatchException}.
     *
     * Este método se activa cuando un parámetro de una URL no coincide con el tipo
     * esperado en la firma del método del controlador. Por ejemplo, si se espera un
     * {@code Long} (como un ID) pero se recibe una cadena de texto como "abc".
     *
     * Registra una advertencia y redirige al usuario a la página de inicio para
     * una experiencia de usuario más fluida en lugar de mostrar una página de
     * error.
     *
     * @param ex La excepción {@link TypeMismatchException} capturada.
     * @return Una cadena de redirección a la URL raíz ("/").
     */
    @ExceptionHandler(TypeMismatchException.class)
    public String handleTypeMismatchException(TypeMismatchException ex) {
        logger.warn("Se detectó un intento de acceder a una URL con un tipo de dato incorrecto. " +
                "Valor: '{}', Tipo requerido: '{}'. Redirigiendo a la página de inicio.",
                ex.getValue(), ex.getRequiredType());
        return "redirect:/";
    }
}