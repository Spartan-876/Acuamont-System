# ETAPA 1: CONSTRUCCION
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# ETAPA 2: EJECUCION (Imagen ligera Alpine)
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Copiamos el jar generado
# Usamos el asterisco *.jar para que no falle si cambia el nombre de la version
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]