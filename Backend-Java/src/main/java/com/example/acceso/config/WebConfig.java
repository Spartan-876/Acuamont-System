package com.example.acceso.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final SessionInterceptor sessionInterceptor;

    public WebConfig(SessionInterceptor sessionInterceptor) {
        this.sessionInterceptor = sessionInterceptor;
    }

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/")
                .setCachePeriod(0);

        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/")
                .setCachePeriod(0);

        registry.addResourceHandler("/iconos/**")
                .addResourceLocations("classpath:/static/iconos/")
                .setCachePeriod(0);

        registry.addResourceHandler("/Imagenes-Web/**")
                .addResourceLocations("classpath:/static/Imagenes-Web/")
                .setCachePeriod(0);
    }

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(sessionInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login",
                        "/logout",
                        "/css/**",
                        "/js/**",
                        "/iconos/**",
                        "/Imagenes-Web/**",
                        "/favicon.ico",
                        "/error",
                        "/PrincipalPage-web",
                        "/slides/PrincipalPage-web",
                        "/slides/api/listar-urls",
                        "/Productos-web",
                        "/Servicios-web",
                        "/Contacto-web",
                        "/Comentarios-web",
                        "/productos/api/listar",
                        "/categorias/api/activas"
                );
    }

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:8080",
                        "http://localhost:3001",
                        "http://localhost:3002",
                        "https://acuamont-system-ek2h.onrender.com",
                        "https://acuamont-system-1.onrender.com",
                        "https://acuamont-system.onrender.com"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}