# Despliegue en Render + TiDB Cloud

Este proyecto conserva la estructura original de NetBeans (`src/java`, `web`) y agrega un build Maven y un Dockerfile para desplegar en Render.

## 1. Crear la base de datos en TiDB Cloud

1. Entra a <https://tidbcloud.com> y crea una instancia **TiDB Cloud Starter** (plan gratuito).
2. En **Connect**, copia:
   - `host`
   - `port` = `4000`
   - `user` (incluye el prefijo, por ejemplo `3KxAbC123.root`)
   - `password`
3. En el **SQL Editor**, ejecuta el contenido de [`database/schema.sql`](database/schema.sql).
4. En **Settings → Networking / Firewall**, permite `0.0.0.0/0` (el plan gratuito de Render no tiene IPs de salida estáticas).

## 2. Variables de entorno

Configura estas variables en Render (o en `render.yaml`):

| Variable | Valor |
|---|---|
| `DB_HOST` | host de TiDB Cloud |
| `DB_PORT` | `4000` |
| `DB_NAME` | `gluco_api` |
| `DB_USER` | usuario de TiDB Cloud |
| `DB_PASSWORD` | password de TiDB Cloud |
| `DB_SSL_MODE` | `VERIFY_IDENTITY` |

Para desarrollo local no hace falta definirlas: se usan los valores por defecto (`127.0.0.1:3306`, `root/root`, `gluco_api`, SSL deshabilitado).

## 3. Compilar localmente

```bash
mvn clean package
```

El WAR queda en `target/API_HealthBlood_512.war`.

También se puede compilar desde NetBeans como siempre (`Clean and Build`).

## 4. Probar con Docker

```bash
docker build -t healthblood-api .
docker run --rm -p 8080:8080 -e PORT=8080 healthblood-api
```

## 5. Desplegar en Render

1. Sube el proyecto a un repositorio de GitHub.
2. En Render: **New → Web Service → Build and deploy from a Git repository**.
3. Selecciona el repositorio.
4. Runtime: **Docker**.
5. Plan: **Free**.
6. Health Check Path: `/api/glucosa/saludar`.
7. Agrega las variables de entorno de la tabla anterior.
8. Deploy.

También puedes usar `render.yaml` desde **New → Blueprint**.

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

## Notas

- El WAR se despliega como `ROOT.war`, por lo que las rutas quedan en `/api/...`.
- Tomcat escucha en el puerto indicado por la variable `PORT` de Render.
- `DB_SSL_MODE=VERIFY_IDENTITY` activa TLS obligatorio para TiDB Cloud. Si tu entorno no valida el certificado, usa `REQUIRED`.
- El plan gratuito de Render suspende el servicio por inactividad (cold start).
