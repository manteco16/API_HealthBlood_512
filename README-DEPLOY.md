# Despliegue en Render

Este proyecto conserva la estructura original de NetBeans (`src/java`, `web`, `nbproject`) y agrega un build Maven y un Dockerfile para desplegar en Render.

- Frontend: `https://<servicio>.onrender.com/`
- API: `https://<servicio>.onrender.com/api/...`
- Health check: `https://<servicio>.onrender.com/api/glucosa/saludar`

## Arquitectura del despliegue

El contenedor incluye **Tomcat 11 + MariaDB 10.11**:

1. El `entrypoint.sh` configura el puerto de Render (`PORT`).
2. Si no hay una base de datos externa configurada, arranca MariaDB dentro del mismo contenedor.
3. Carga `database/schema.sql` y `database/data.sql` automáticamente (son idempotentes).
4. Arranca Tomcat con el WAR en `ROOT.war`.

Esto hace que la API funcione desde el primer arranque **sin depender de un servicio externo**.

> **Importante:** en el plan gratuito de Render el disco es efímero. Los datos creados con `POST` se pierden cuando el servicio se reinicia o se vuelve a desplegar. Los datos de ejemplo se vuelven a cargar automáticamente en cada arranque. Para persistencia real, conecta una base de datos externa (ver sección TiDB Cloud).

## 1. Desplegar en Render

### Opción A: Blueprint (recomendado)

1. Sube el proyecto a GitHub (rama `main`).
2. En Render: **New → Blueprint**.
3. Conecta GitHub y selecciona el repositorio.
4. Render leerá `render.yaml`.
5. Clic en **Apply / Deploy**.

No hay que configurar variables obligatorias: el contenedor usa MariaDB embebida.

### Opción B: Web Service manual

1. En Render: **New → Web Service → Public Git repository**.
2. Pega la URL del repositorio.
3. Runtime: **Docker**.
4. Plan: **Free**.
5. Health Check Path: `/api/glucosa/saludar`.
6. Clic en **Create Web Service**.

## 2. Variables de entorno

| Variable | Valor por defecto | Descripción |
|---|---|---|
| `DB_SSL_MODE` | `DISABLED` | Modo SSL de la conexión JDBC |
| `JAVA_OPTS` | `-Xms64m -Xmx160m -XX:MaxMetaspaceSize=96m` | Límite de memoria para Render Free |
| `PORT` | `10000` (Render) | Puerto HTTP; el entrypoint lo aplica a Tomcat |

Si `DB_HOST` no se define (o es `127.0.0.1`/`localhost`), se usa MariaDB embebida.

## 3. Compilar y probar localmente

Con Docker (recomendado, no requiere Maven instalado):

```bash
docker build -t healthblood-api .
docker run --rm -p 8080:8080 -e PORT=8080 healthblood-api
```

Con Maven:

```bash
mvn clean package
```

El WAR queda en `target/API_HealthBlood_512.war`.

También se puede compilar desde NetBeans como siempre (`Clean and Build`).

## 4. Endpoints para verificar

```bash
curl https://<servicio>.onrender.com/api/glucosa/saludar
curl https://<servicio>.onrender.com/api/glucosa/getAllUsuarios
curl https://<servicio>.onrender.com/api/glucosa/getAllMedidas
curl "https://<servicio>.onrender.com/api/glucosa/getMedidasByUsuarioId?idUsuario=1"
curl -X POST https://<servicio>.onrender.com/api/usuario/saveUsuario \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "nombre=Ana&primerApellido=Perez&segundoApellido=Lopez&email=ana@example.com"
```

Resultado esperado de los GET:

```json
[{"idUsuario":1,"nombre":"Ana","primerApellido":"Perez","segundoApellido":"Lopez","email":"ana@example.com"}]
```

## 5. Base de datos externa opcional (TiDB Cloud Starter)

Para que los datos persistan entre reinicios, se puede usar TiDB Cloud Starter (MySQL compatible):

1. Entra a <https://tidbcloud.com/> y crea una instancia **Starter** (gratuita).
2. En **Connect → Public**, copia `host`, `port` (`4000`), `user` (con prefijo) y `password`.
3. En el **SQL Editor**, ejecuta `database/schema.sql` y `database/data.sql`.
4. En **Networking / Firewall**, permite `0.0.0.0/0` (Render Free no tiene IPs de salida fijas).
5. En Render → tu servicio → **Environment**, agrega:

| Variable | Valor |
|---|---|
| `DB_HOST` | host de TiDB Cloud |
| `DB_PORT` | `4000` |
| `DB_NAME` | `gluco_api` |
| `DB_USER` | usuario de TiDB Cloud (con prefijo) |
| `DB_PASSWORD` | password de TiDB Cloud |
| `DB_SSL_MODE` | `VERIFY_IDENTITY` (si falla TLS, usa `REQUIRED`) |

Al definir `DB_HOST` con un valor distinto de `127.0.0.1`, el entrypoint **omite MariaDB** y usa la base externa.

## 6. Solución de problemas

### Error de TLS / SSLHandshakeException

Cambia `DB_SSL_MODE` a `REQUIRED` y redeploy. TiDB Cloud Starter exige TLS; `REQUIRED` cifra la conexión sin validar el certificado. `VERIFY_IDENTITY` es más estricto y debería funcionar con Java 21 porque incluye Let's Encrypt (ISRG Root X1/X2).

### Error de conexión / Access denied

- Verifica que `DB_USER` incluya el prefijo de TiDB.
- Verifica que `DB_PASSWORD` sea el generado en TiDB Cloud.
- Verifica que el firewall de TiDB permita `0.0.0.0/0`.
- Revisa los logs de Render: allí aparece el stack trace de `ConnectionMysql`.

### El servicio tarda en responder

El plan gratuito de Render suspende el servicio por inactividad. La primera petición después de un tiempo puede tardar ~50 segundos (cold start). El arranque incluye MariaDB + Tomcat.

### El health check falla

- Confirma que el WAR se desplegó como `ROOT.war` (el Dockerfile ya lo hace).
- Confirma que la ruta es `/api/glucosa/saludar`.
- Revisa los logs de arranque de Tomcat en Render.

## Notas

- El WAR se despliega como `ROOT.war`, por lo que las rutas quedan en `/api/...`.
- Tomcat escucha en el puerto indicado por la variable `PORT` de Render.
- `JAVA_OPTS` limita la memoria para el plan gratuito de Render (512 MB).
- `database/data.sql` usa `INSERT IGNORE` y es compatible con MariaDB y TiDB Cloud Starter.
- `database/schema.sql` usa `CREATE TABLE IF NOT EXISTS`, por lo que se puede ejecutar varias veces.
- La imagen usa `tomcat:11-jdk21-temurin-noble` (Ubuntu 24.04) por estabilidad de repositorios.
