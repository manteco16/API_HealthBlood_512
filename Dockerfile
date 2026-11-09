# syntax=docker/dockerfile:1

# Etapa 1: compilar el WAR con Maven (estructura NetBeans original)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
COPY web ./web
RUN mvn -B -q clean package -DskipTests

# Etapa 2: ejecutar en Tomcat 11 (Jakarta EE 11)
FROM tomcat:11-jdk21-temurin

# Limita la memoria para el plan gratuito de Render (512 MB)
ENV JAVA_OPTS="-Xms64m -Xmx256m -XX:MaxMetaspaceSize=128m"

RUN rm -rf /usr/local/tomcat/webapps/*

COPY --from=build /app/target/API_HealthBlood_512.war /usr/local/tomcat/webapps/ROOT.war
COPY docker/entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh

EXPOSE 8080

ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
