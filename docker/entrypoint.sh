#!/bin/sh
set -e

# Render define PORT (por defecto 10000). Tomcat usa 8080.
PORT="${PORT:-8080}"

sed -i "s/port=\"8080\"/port=\"${PORT}\"/" /usr/local/tomcat/conf/server.xml

exec catalina.sh run
