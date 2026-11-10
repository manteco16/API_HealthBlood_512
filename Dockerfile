# syntax=docker/dockerfile:1

# Etapa 1: compilar el WAR con Maven (estructura NetBeans original)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
COPY web ./web
RUN mvn -B -q clean package -DskipTests

# Etapa 2: Tomcat 11 + MariaDB embebida (modo autonomo para Render)
FROM tomcat:11-jdk21-temurin-noble

# Limita la memoria para el plan gratuito de Render (512 MB)
ENV JAVA_OPTS="-Xms64m -Xmx160m -XX:MaxMetaspaceSize=96m"

# MariaDB permite que la API funcione sin una base de datos externa.
# Si se define DB_HOST con otro valor, el entrypoint omite MariaDB y usa la BD externa.
RUN printf '#!/bin/sh\nexit 101\n' > /usr/sbin/policy-rc.d \
 && chmod +x /usr/sbin/policy-rc.d \
 && find /etc/apt -type f \( -name '*.list' -o -name '*.sources' \) -exec sed -i 's|http://archive.ubuntu.com|https://archive.ubuntu.com|g; s|http://security.ubuntu.com|https://security.ubuntu.com|g; s|http://ports.ubuntu.com|https://ports.ubuntu.com|g' {} + \
 && apt-get update \
 && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends mariadb-server mariadb-client \
 && rm -rf /var/lib/apt/lists/* /var/lib/mysql/* \
 && rm -f /usr/sbin/policy-rc.d

RUN rm -rf /usr/local/tomcat/webapps/*

COPY --from=build /app/target/API_HealthBlood_512.war /usr/local/tomcat/webapps/ROOT.war
COPY docker/my.cnf /etc/mysql/conf.d/render.cnf
COPY database/schema.sql /opt/db/schema.sql
COPY database/data.sql /opt/db/data.sql
COPY docker/entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh

EXPOSE 8080

ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
