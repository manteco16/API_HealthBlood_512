#!/bin/sh
set -e

# Render define PORT (por defecto 10000). Tomcat usa 8080 en server.xml.
PORT="${PORT:-8080}"

echo "Starting Tomcat on port ${PORT}"

sed -i "s/port=\"8080\"/port=\"${PORT}\"/" /usr/local/tomcat/conf/server.xml

if ! grep -q "port=\"${PORT}\"" /usr/local/tomcat/conf/server.xml; then
  echo "ERROR: no se pudo configurar el puerto ${PORT} en server.xml" >&2
  exit 1
fi

exec catalina.sh run
