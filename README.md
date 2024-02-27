# SoVote

To start/recreate Postgres DB: `./gradlew startTestDb`

To generate jooq classes: `./gradlew generateJooq`

To run tests: `./gradlew build`

To run the server for development: `./gradlew :service:run`

To push docker image: `./gradlew jib`

Relies on https://github.com/juggernaut0/auth for auth.

## TODO
* Add option to show poll creators email
* Dark mode
