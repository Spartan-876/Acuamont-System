# ETAPA 1: Construcción (Build)
# Usamos una imagen de Maven que ya tenga Java 21 instalado
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# ETAPA 2: Ejecución (Run)
# Usamos una imagen ligera de Java 21 para correr la app
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiamos el .jar generado en la etapa anterior
# ⚠️ Asegúrate de que el nombre coincida con tu pom.xml
COPY --from=build /app/target/acceso-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]