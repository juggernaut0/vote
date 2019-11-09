#!/bin/bash
set -e

./gradlew :ui:webpack
docker run --name web --rm -p 80:80 -v "$PWD/nginx.conf:/etc/nginx/nginx.conf:ro" -v "$PWD/ui/build/web:/etc/nginx/html:ro" -d nginx
echo "Nginx started as web"
