#!/bin/env bash
set -e

docker run --name db --rm --net=host -v "$PWD/init_db.sql:/docker-entrypoint-initdb.d/init_db.sql:ro" -d -p 5432:5432 postgres:11
echo "Postgres started with name 'db'"
sleep 5
./gradlew :dbmigrate:run
