-- Esquema compatible con TiDB Cloud Starter (MySQL-compatible)
-- Ejecutar en el SQL Editor de TiDB Cloud.

CREATE DATABASE IF NOT EXISTS gluco_api;
USE gluco_api;

CREATE TABLE IF NOT EXISTS usuario (
    idUsuario       INT NOT NULL AUTO_INCREMENT,
    nombre          VARCHAR(40) NOT NULL,
    primerApellido  VARCHAR(40) NOT NULL,
    segundoApellido VARCHAR(40) DEFAULT NULL,
    email           VARCHAR(50) NOT NULL,
    PRIMARY KEY (idUsuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS medida (
    idMedida  INT NOT NULL AUTO_INCREMENT,
    idUsuario INT NOT NULL,
    glucosa   INT NOT NULL,
    fechaHora DATETIME DEFAULT NULL,
    contexto  VARCHAR(40) DEFAULT NULL,
    notas     VARCHAR(100) DEFAULT NULL,
    PRIMARY KEY (idMedida),
    KEY idx_medida_usuario (idUsuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- TiDB no aplica las FOREIGN KEY por defecto, por eso se crea solo el indice.
-- La integridad referencial se valida en la aplicacion.

-- Datos de ejemplo opcionales:
-- INSERT INTO usuario (nombre, primerApellido, segundoApellido, email)
-- VALUES ('Ana', 'Perez', 'Lopez', 'ana@example.com');
--
-- INSERT INTO medida (idUsuario, glucosa, fechaHora, contexto, notas)
-- VALUES (1, 95, NOW(), 'ayunas', 'sin sintomas');
