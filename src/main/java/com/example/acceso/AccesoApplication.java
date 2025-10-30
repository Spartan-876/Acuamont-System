package com.example.acceso;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal y punto de entrada para la aplicación Spring Boot.
 * <p>
 * La anotación {@link SpringBootApplication} es una anotación de conveniencia
 * que
 * agrega lo siguiente:
 * <ul>
 * <li>{@link org.springframework.context.annotation.Configuration @Configuration}:
 * Etiqueta la clase como una fuente de definiciones de beans para el contexto
 * de la aplicación.</li>
 * <li>{@link org.springframework.boot.autoconfigure.EnableAutoConfiguration @EnableAutoConfiguration}:
 * Le dice a Spring Boot que comience a agregar beans basados en la
 * configuración de la ruta de clases, otros beans y varias configuraciones de
 * propiedades.</li>
 * <li>{@link org.springframework.context.annotation.ComponentScan @ComponentScan}:
 * Le dice a Spring que busque otros componentes, configuraciones y servicios en
 * el paquete 'com.example.acceso', permitiéndole encontrar los controladores,
 * servicios, etc.</li>
 * </ul>
 */
@SpringBootApplication
public class AccesoApplication {

	/**
	 * El método principal que sirve como punto de entrada para la aplicación.
	 * <p>
	 * Este método utiliza {@link SpringApplication#run(Class, String...)} para
	 * lanzar la aplicación.
	 *
	 * @param args Argumentos de línea de comandos que se pueden pasar al iniciar la
	 *             aplicación.
	 */
	public static void main(String[] args) {
		SpringApplication.run(AccesoApplication.class, args);
	}
}
