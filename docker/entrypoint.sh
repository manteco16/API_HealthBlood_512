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

DB_HOST_VALUE="${DB_HOST:-127.0.0.1}"

if [ "$DB_HOST_VALUE" = "127.0.0.1" ] || [ "$DB_HOST_VALUE" = "localhost" ]; then
  echo "Base de datos externa no configurada. Iniciando MariaDB embebida..."

  mkdir -p /run/mysqld /var/lib/mysql
  chown -R mysql:mysql /run/mysqld /var/lib/mysql

  mariadb-install-db --user=mysql --datadir=/var/lib/mysql --skip-test-db >/dev/null 2>&1

  mariadbd --user=mysql \
    --datadir=/var/lib/mysql \
    --socket=/run/mysqld/mysqld.sock \
    --pid-file=/run/mysqld/mysqld.pid \
    --bind-address=127.0.0.1 \
    --port=3306 &

  DB_PID=$!
  READY=0
  i=0
  while [ "$i" -lt 60 ]; do
    if mariadb-admin --socket=/run/mysqld/mysqld.sock ping >/dev/null 2>&1; then
      READY=1
      break
    fi
    i=$((i + 1))
    sleep 1
  done

  if [ "$READY" != "1" ]; then
    echo "ERROR: MariaDB no arranco" >&2
    kill "$DB_PID" 2>/dev/null || true
    exit 1
  fi

  # Esquema y datos de ejemplo (idempotentes).
  mariadb --socket=/run/mysqld/mysqld.sock -uroot < /opt/db/schema.sql
  mariadb --socket=/run/mysqld/mysqld.sock -uroot < /opt/db/data.sql

  # Credenciales que usa la aplicacion por defecto (root/root en 127.0.0.1:3306).
  mariadb --socket=/run/mysqld/mysqld.sock -uroot <<'SQL'
ALTER USER 'root'@'localhost' IDENTIFIED VIA mysql_native_password USING PASSWORD('root');
CREATE USER IF NOT EXISTS 'root'@'127.0.0.1' IDENTIFIED BY 'root';
GRANT ALL PRIVILEGES ON *.* TO 'root'@'127.0.0.1' WITH GRANT OPTION;
FLUSH PRIVILEGES;
SQL

  echo "MariaDB lista con los datos de ejemplo."
else
  echo "Usando base de datos externa en ${DB_HOST_VALUE}:${DB_PORT:-3306}."
fi

exec catalina.sh run
