# ---------- Build ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -e -DskipTests package

# ---------- Runtime ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
# (opcional) creo un usuario no root
RUN useradd -ms /bin/bash appuser
USER appuser
# copio el jar con capas de Spring Boot
COPY --from=build /app/target/*.jar /app/app.jar

# Puerto por defecto
EXPOSE 8080

# Env para que Spring apunte al contenedor mongo
ENV SPRING_DATA_MONGODB_URI="mongodb://mongo:27017/pvdb"

# Levanto la app
ENTRYPOINT ["java","-jar","/app/app.jar"]