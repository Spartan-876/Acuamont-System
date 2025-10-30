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

@Service
public class ServicioAutenticacionDosPasos {

    // Genera un nuevo secreto de 64 bits compatible con Google Authenticator.
    public String generarNuevoSecreto() {
        SecretGenerator secretGenerator = new DefaultSecretGenerator(64);
        return secretGenerator.generate();
    }

    // Genera la URI de datos para una imagen QR en formato PNG.
    // Esta URI se puede poner directamente en el atributo `src` de una etiqueta <img>.
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
            // Manejar la excepción, por ejemplo, logueándola.
            throw new RuntimeException("Error al generar el código QR", e);
        }

        return Utils.getDataUriForImage(imageData, generator.getImageMimeType());
    }

    // Verifica si un código proporcionado es válido para un secreto dado.
    public boolean esCodigoValido(String secreto, String codigo) {
        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator();
        CodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
        return verifier.isValidCode(secreto, codigo);
    }
}
