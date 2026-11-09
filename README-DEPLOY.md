# Despliegue en Render + TiDB Cloud Starter

Este proyecto conserva la estructura original de NetBeans (`src/java`, `web`, `nbproject`) y agrega un build Maven y un Dockerfile para desplegar en Render.

- Frontend: `https://<servicio>.onrender.com/`
- API: `https://<servicio>.onrender.com/api/...`
- Health check: `https://<servicio>.onrender.com/api/glucosa/saludar`

## 1. Crear la base de datos en TiDB Cloud Starter

1. Entra a <https://tidbcloud.com/> e inicia sesión.
2. Clic en **Create Resource** o **Create Cluster**.
3. Selecciona el plan **Starter** (gratuito).
4. Elige una región cercana a Render (por ejemplo `us-west-2` / Oregon si está disponible).
5. Nombre sugerido: `healthblood`.
6. Espera a que el estado sea **Available**.
7. Clic en **Connect** y selecciona el endpoint **Public**.
8. Copia estos datos:
   - `host` (ej. `gateway01.us-west-2.prod.aws.tidbcloud.com`)
   - `port` = `4000`
   - `user` (incluye el prefijo, ej. `3KxAbC123.root`)
   - `password` (si no existe, usa **Generate Password**; se muestra una sola vez)
9. En **SQL Editor**, ejecuta:
   - [`database/schema.sql`](database/schema.sql)
   - [`database/data.sql`](database/data.sql)
10. Verifica:
    ```sql
    SELECT * FROM gluco_api.usuario;
    SELECT * FROM gluco_api.medida;
    ```
11. En **Networking / Firewall** (o **IP Access List**), permite `0.0.0.0/0` para el endpoint público.

> Render Free no tiene IPs de salida fijas, por eso el firewall de TiDB debe permitir todas las IPs. Es un requisito del plan gratuito.

## 2. Variables de entorno

Configura estas variables en Render (o en `render.yaml`):

| Variable | Valor |
|---|---|
| `DB_HOST` | host de TiDB Cloud |
| `DB_PORT` | `4000` |
| `DB_NAME` | `gluco_api` |
| `DB_USER` | usuario de TiDB Cloud (con prefijo) |
| `DB_PASSWORD` | password de TiDB Cloud |
| `DB_SSL_MODE` | `VERIFY_IDENTITY` (si falla TLS, usa `REQUIRED`) |
| `JAVA_OPTS` | `-Xms64m -Xmx256m -XX:MaxMetaspaceSize=128m` |

Para desarrollo local no hace falta definirlas: se usan los valores por defecto (`127.0.0.1:3306`, `root/root`, `gluco_api`, SSL deshabilitado).

> `DB_HOST`, `DB_USER` y `DB_PASSWORD` están marcadas con `sync: false` en `render.yaml`, así que Render te las pedirá al crear el Blueprint y no se guardan en el repositorio.

## 3. Compilar localmente

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

## 4. Probar con Docker + MySQL local

```bash
# 1. MySQL temporal
docker run --rm -d --name healthblood-mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=gluco_api \
  -p 3306:3306 mysql:8

# 2. Cargar esquema y datos (espera unos segundos a que MySQL arranque)
docker exec -i healthblood-mysql mysql -uroot -proot < database/schema.sql
docker exec -i healthblood-mysql mysql -uroot -proot < database/data.sql

# 3. Aplicación
docker build -t healthblood-api .
docker run --rm -p 8080:8080 \
  -e PORT=8080 \
  -e DB_HOST=host.docker.internal \
  -e DB_PORT=3306 \
  -e DB_NAME=gluco_api \
  -e DB_USER=root \
  -e DB_PASSWORD=root \
  -e DB_SSL_MODE=DISABLED \
  healthblood-api
```

Prueba:

```bash
curl http://localhost:8080/api/glucosa/saludar
curl http://localhost:8080/api/glucosa/getAllUsuarios
curl http://localhost:8080/api/glucosa/getAllMedidas
```

## 5. Desplegar en Render

### Opción A: Blueprint (recomendado)

1. Sube el proyecto a GitHub.
2. En Render: **New → Blueprint**.
3. Conecta GitHub y selecciona el repositorio.
4. Render leerá `render.yaml`.
5. Completa `DB_HOST`, `DB_USER` y `DB_PASSWORD`.
6. Confirma `DB_PORT=4000`, `DB_NAME=gluco_api` y `DB_SSL_MODE=VERIFY_IDENTITY`.
7. Clic en **Apply / Deploy**.

### Opción B: Web Service manual

1. En Render: **New → Web Service → Public Git repository**.
2. Pega la URL del repositorio.
3. Runtime: **Docker**.
4. Plan: **Free**.
5. Health Check Path: `/api/glucosa/saludar`.
6. Agrega las variables de entorno de la tabla anterior.
7. Clic en **Create Web Service**.

## 6. Endpoints para verificar

Con el servicio desplegado (reemplaza `<servicio>`):

```bash
curl https://<servicio>.onrender.com/api/glucosa/saludar
curl https://<servicio>.onrender.com/api/glucosa/getAllUsuarios
curl https://<servicio>.onrender.com/api/glucosa/getAllMedidas
curl "https://<servicio>.onrender.com/api/glucosa/getMedidasByUsuarioId?idUsuario=1"
curl -X POST https://<servicio>.onrender.com/api/usuario/saveUsuario \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "nombre=Ana&primerApellido=Perez&segundoApellido=Lopez&email=ana@example.com"
```

## 7. Solución de problemas

### Error de TLS / SSLHandshakeException

Cambia `DB_SSL_MODE` a `REQUIRED` en Render y redeploy:

```
DB_SSL_MODE=REQUIRED
```

TiDB Cloud Starter exige TLS; `REQUIRED` cifra la conexión sin validar el certificado. `VERIFY_IDENTITY` es más estricto y debería funcionar con Java 21 porque incluye Let's Encrypt (ISRG Root X1/X2).

### Error de conexión / Access denied

- Verifica que `DB_USER` incluya el prefijo de TiDB.
- Verifica que `DB_PASSWORD` sea el generado en TiDB Cloud.
- Verifica que el firewall de TiDB permita `0.0.0.0/0`.
- Revisa los logs de Render: allí aparece el stack trace de `ConnectionMysql`.

### El servicio tarda en responder

El plan gratuito de Render suspende el servicio por inactividad. La primera petición después de un tiempo puede tardar ~50 segundos (cold start).

### El health check falla

- Confirma que el WAR se desplegó como `ROOT.war` (el Dockerfile ya lo hace).
- Confirma que la ruta es `/api/glucosa/saludar`.
- Revisa los logs de arranque de Tomcat en Render.

## Notas

- El WAR se despliega como `ROOT.war`, por lo que las rutas quedan en `/api/...`.
- Tomcat escucha en el puerto indicado por la variable `PORT` de Render.
- `JAVA_OPTS` limita la memoria para el plan gratuito de Render (512 MB).
- `database/data.sql` usa `INSERT IGNORE` y es compatible con TiDB Cloud Starter.
- `database/schema.sql` usa `CREATE TABLE IF NOT EXISTS`, por lo que se puede ejecutar varias veces.
