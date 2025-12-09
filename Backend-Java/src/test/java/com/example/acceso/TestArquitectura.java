package com.example.acceso;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(packages = "com.example.acceso")
public class TestArquitectura {

    // Definiciones de Paquetes
    private static final String CONTROLLER = "..controller..";
    private static final String SERVICE_API = "..Interfaces..";
    private static final String SERVICE_IMPL = "..Implements..";
    private static final String REPOSITORY = "..repository..";
    private static final String MODEL = "..model..";
    private static final String DTO = "..DTO..";

    private final JavaClasses classes = new ClassFileImporter().importPackages("com.example.acceso");

    // REGLAS DE CAPAS
    @Test
    void test_arquitectura_en_capas() {
        ArchRule reglaCapas = layeredArchitecture()
                .consideringOnlyDependenciesInLayers()

                .layer("Controller").definedBy(CONTROLLER)
                .layer("Interfaces").definedBy(SERVICE_API)
                .layer("Implements").definedBy(SERVICE_IMPL)
                .layer("Repository").definedBy(REPOSITORY)
                .layer("Model").definedBy(MODEL)
                .layer("DTO").definedBy(DTO)

                .whereLayer("Controller").mayOnlyAccessLayers("Interfaces","DTO")
                .whereLayer("Interfaces").mayOnlyAccessLayers("DTO", "Model")
                .whereLayer("Implements").mayOnlyAccessLayers("Interfaces", "Repository", "Model", "DTO")
                .whereLayer("Repository").mayOnlyAccessLayers("Model")
                .whereLayer("Model").mayNotAccessAnyLayer()
                .whereLayer("DTO").mayNotAccessAnyLayer();

        reglaCapas.check(classes);
    }

    // REGLAS ESPECÍFICAS DE CONTROLADORES
    @Test
    void test_reglas_controlador() {
        ArchRule reglaControladores = classes()
                .that().resideInAPackage(CONTROLLER)
                .should().beAnnotatedWith(RestController.class)
                .orShould().beAnnotatedWith(Controller.class)
                .orShould().beAnnotatedWith(ControllerAdvice.class);

        reglaControladores.check(classes);
    }

    @Test
    void test_controladores_no_deben_acceder_a_repositorios() {
        ArchRule regla = noClasses()
                .that().resideInAPackage(CONTROLLER)
                .should().dependOnClassesThat().resideInAPackage(REPOSITORY);

        regla.check(classes);
    }

    // REGLAS ESPECÍFICAS DE SERVICIOS
    @Test
    void test_reglas_servicio() {
        ArchRule reglaServicios = classes()
                .that().resideInAPackage(SERVICE_IMPL)
                .and().areNotInterfaces()
                .should().beAnnotatedWith(Service.class);

        reglaServicios.check(classes);
    }

    @Test
    void test_servicios_no_deben_acceder_a_api_web() {
        ArchRule regla = noClasses()
                .that().resideInAPackage(SERVICE_IMPL)
                .should().dependOnClassesThat().resideInAPackage("jakarta.servlet.."); // O 'javax.servlet..'

        regla.check(classes);
    }
}