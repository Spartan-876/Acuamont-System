package com.example.acceso.service;

import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import dev.samstevens.totp.util.Utils;
import org.springframework.stereotype.Service;

/**
 * Servicio para gestionar la lógica de la autenticación de dos factores (2FA)
 * utilizando el protocolo TOTP (Time-Based One-Time Password).
 *
 * Esta clase encapsula la funcionalidad de la librería `totp-java-lib` para:
 * 1. Generar secretos compatibles con aplicaciones como Google Authenticator.
 * 2. Crear URIs de datos para códigos QR que facilitan la configuración.
 * 3. Verificar los códigos TOTP ingresados por el usuario.
 */
@Service
public class ServicioAutenticacionDosPasos {

    /**
     * Genera un nuevo secreto aleatorio de 64 bits.
     *
     * Este secreto es la clave compartida entre el servidor y la aplicación de
     * autenticación del usuario.
     *
     * @return Una cadena de texto que representa el secreto en formato Base32.
     */
    public String generarNuevoSecreto() {
        SecretGenerator secretGenerator = new DefaultSecretGenerator(64);
        return secretGenerator.generate();
    }

    /**
     * Genera una URI de datos para una imagen de código QR en formato PNG.
     *
     * Esta URI puede ser usada directamente en el atributo `src` de una etiqueta
     * `<img>`
     * en el frontend para mostrar el QR que el usuario escaneará con su app de
     * autenticación.
     *
     * @param secreto El secreto Base32 generado para el usuario.
     * @param email   El correo o identificador del usuario que se mostrará en la
     *                app de autenticación.
     * @param issuer  El nombre de la aplicación o empresa (ej. "MiEmpresa") que se
     *                mostrará en la app.
     * @return Una cadena de texto con la URI de datos de la imagen QR.
     * @throws RuntimeException si ocurre un error durante la generación del QR.
     */
    public String generarUriDatosQr(String secreto, String email, String issuer) {
        QrData data = new QrData.Builder()
                .label(email)
                .secret(secreto)
                .issuer(issuer)
                .algorithm(HashingAlgorithm.SHA1) // Algoritmo estándar
                .digits(6)
                .period(30)
                .build();

        QrGenerator generator = new ZxingPngQrGenerator();
        byte[] imageData;
        try {
            imageData = generator.generate(data);
        } catch (QrGenerationException e) {
            throw new RuntimeException("Error al generar el código QR", e);
        }

        return Utils.getDataUriForImage(imageData, generator.getImageMimeType());
    }

    /**
     * Verifica si un código TOTP proporcionado por el usuario es válido para un
     * secreto específico.
     *
     * Compara el código ingresado con el que se espera en el momento actual,
     * permitiendo una pequeña ventana de tiempo para compensar desincronizaciones.
     *
     * @param secreto El secreto Base32 del usuario.
     * @param codigo  El código de 6 dígitos ingresado por el usuario.
     * @return {@code true} si el código es válido, {@code false} en caso contrario.
     */
    public boolean esCodigoValido(String secreto, String codigo) {
        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator();
        CodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
        return verifier.isValidCode(secreto, codigo);
    }
}
