-- Datos de ejemplo compatibles con TiDB Cloud Starter.
-- Ejecutar despues de database/schema.sql.
-- INSERT IGNORE permite ejecutar este archivo varias veces sin errores de duplicados.

USE gluco_api;

INSERT IGNORE INTO usuario (idUsuario, nombre, primerApellido, segundoApellido, email)
VALUES (1, 'Ana', 'Perez', 'Lopez', 'ana@example.com');

INSERT IGNORE INTO medida (idMedida, idUsuario, glucosa, fechaHora, contexto, notas)
VALUES (1, 1, 95, NOW(), 'ayunas', 'sin sintomas');
