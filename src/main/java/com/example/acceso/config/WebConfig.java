package com.example.acceso.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración principal de Spring MVC para la aplicación.
 *
 * Esta clase implementa {@link WebMvcConfigurer} para personalizar
 * el comportamiento de Spring Web. Se encarga de:
 * 1. Mapear rutas de URL a ubicaciones de archivos estáticos (CSS, JS, imágenes).
 * 2. Mapear rutas de URL a directorios de archivos subidos (fotos de productos).
 * 3. Registrar interceptores de sesión para la seguridad de las rutas.
 * 4. Configurar CORS para los endpoints de la API.
 */

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Interceptor personalizado para validar la sesión del usuario.
     */
    private final SessionInterceptor sessionInterceptor;

    /**
     * Ruta del directorio en el sistema de archivos donde se guardan las fotos de productos.
     * Inyectado desde {@code application.properties} (llave: file.upload-dir).
     */
    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * Ruta del directorio en el sistema de archivos donde se guardan las fotos de productos.
     * Inyectado desde {@code application.properties} (llave: file.upload-dir).
     */
    @Value("${files.iconos-dir}")
    private String iconosDir;

    /**
     * Ruta del directorio para las imágenes generales del sitio web.
     * Inyectado desde {@code application.properties} (llave: files.imagenesWeb-dir).
     */
    @Value("${files.imagenesWeb-dir}")
    private String imagenesWebDir;

    /**
     * Ruta del directorio para las imágenes del carrusel (slide) de inicio.
     * Inyectado desde {@code application.properties} (llave: files.imagenesSlide-dir).
     */
    @Value("${files.imagenesSlide-dir}")
    private String imagenesSlideDir;

    /**
     * Constructor para inyectar las dependencias de configuración.
     *
     * @param sessionInterceptor El interceptor de sesión que se registrará.
     */
    public WebConfig(SessionInterceptor sessionInterceptor) {
        this.sessionInterceptor = sessionInterceptor;
    }

    /**
     * Configura los manejadores de recursos estáticos.
     *
     * Mapea las rutas URL (ej. "/css/**") a sus ubicaciones físicas, ya sea
     * dentro del classpath (para CSS, JS) o en el sistema de archivos externo
     * (para fotos de productos, iconos, etc.).
     * Se deshabilita el caché (setCachePeriod(0)) para el entorno de desarrollo.
     *
     * @param registry El registro donde se añaden los manejadores de recursos.
     */
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/")
                .setCachePeriod(0);

        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/")
                .setCachePeriod(0);

        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/")
                .setCachePeriod(0);

        registry.addResourceHandler("/Fotos-Productos/**")
                .addResourceLocations("file:" + uploadDir)
                .setCachePeriod(0);


        registry.addResourceHandler("/iconos/**")
                .addResourceLocations("file:"+ iconosDir)
                .setCachePeriod(0);

        registry.addResourceHandler("/Imagenes-Web/**")
                .addResourceLocations("file:"+imagenesWebDir)
                .setCachePeriod(0);

        registry.addResourceHandler("/slide-Inicio/**")
                .addResourceLocations("file:"+imagenesSlideDir)
                .setCachePeriod(0);
    }

    /**
     * Registra los interceptores de la aplicación.
     *
     * Añade el {@link SessionInterceptor} para proteger todas las rutas (`/**`).
     * Se definen explícitamente las rutas a "excluir" de esta intercepción,
     * como la página de login, los recursos estáticos (CSS/JS), las APIs públicas
     * y las páginas públicas del sitio web.
     *
     * @param registry El registro donde se añade el interceptor.
     */
    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(sessionInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/login",
                        "/logout",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/iconos/**",
                        "/Imagenes-Web/**",
                        "/slide-Inicio/**",
                        "/productos/api/**",
                        "/Fotos-Productos/**",
                        "/error",
                        "/favicon.ico",
                        "/PrincipalPage-web",
                        "/Servicios-web",
                        "/Productos-web",
                        "/Contacto-web"
                        );

    }

    /**
     * Configura el Cross-Origin Resource Sharing (CORS) para la API.
     *
     * Permite que las peticiones desde orígenes específicos (ej. localhost:8080)
     * accedan a los endpoints de la API de usuarios (`/usuarios/api/**`),
     * especificando los métodos HTTP permitidos (GET, POST, etc.).
     *
     * @param registry El registro donde se añade la configuración CORS.
     */
    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/usuarios/api/**")
                .allowedOrigins("http://localhost:8080")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

}