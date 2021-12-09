# SoVote

To start/restart Postgres and run migrations: `./drop_and_migrate.sh`

To generate jooq classes, make sure DB is up and migrated. Then run `./gradlew generateJooq`

To run tests: `./gradlew build`

To run the server for development: `./gradlew :service:run`

To create production-ready builds: `./gradlew clean build dockerBuild`

Relies on https://github.com/juggernaut0/auth for auth.

## TODO
* Add option to show poll creators email
* Dark mode
