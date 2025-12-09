package com.example.acceso.service.Implements;

import com.cloudinary.Cloudinary;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    public String subirImagen(MultipartFile file, String carpetaNombre) {
        try {
            Map params = ObjectUtils.asMap(
                    "folder", carpetaNombre,
                    "resource_type", "image");

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);

            return uploadResult.get("secure_url").toString();

        } catch (IOException e) {
            throw new RuntimeException("Error al subir imagen a Cloudinary: " + e.getMessage());
        }
    }

    public Map eliminarImagen(String id, Map options) {
        try {
            return cloudinary.uploader().destroy(id, options);
        } catch (IOException e) {
            throw new RuntimeException("Error al eliminar imagen de Cloudinary");
        }
    }

    /**
     * Lista todas las imágenes que estén dentro de una carpeta específica en
     * Cloudinary.
     * Nota: Usa la Admin API, que tiene límites de uso (Rate Limits).
     */
    public List<String> listarImagenesDeCarpeta(String nombreCarpeta) {
        try {
            ApiResponse response = cloudinary.api().resources(ObjectUtils.asMap(
                    "type", "upload",
                    "prefix", nombreCarpeta + "/",
                    "max_results", 10
            ));

            List<Map> resources = (List<Map>) response.get("resources");

            return resources.stream()
                    .map(res -> res.get("secure_url").toString())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // En CloudinaryService.java

    /**
     * Sube una imagen con un nombre específico (Public ID).
     * Útil para logos o imágenes fijas que se sobrescriben.
     */
    public String subirImagenConNombreFijo(MultipartFile file, String nombreFijo) {
        try {
            Map params = ObjectUtils.asMap(
                    "public_id", nombreFijo,
                    "folder", "iconos",
                    "overwrite", true,
                    "resource_type", "image"
            );

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
            return uploadResult.get("secure_url").toString();

        } catch (IOException e) {
            throw new RuntimeException("Error al subir logo: " + e.getMessage());
        }
    }


    public String obtenerUrlImagen(String publicId) {
        try {
            return cloudinary.url().secure(true).generate("iconos/" + publicId);
        } catch (Exception e) {
            return null;
        }
    }

}