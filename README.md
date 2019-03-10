# SoVote

One-time setup: `./gradlew npmInstall`

To start Postgres and run migrations: `./start_db.sh`

To start Nginx: `./start_web.sh`. Make sure *not* to `:ui:clean` while 
Nginx is running, or else it will start returning errors.

To generate jooq classes, make sure DB is up and migrated. Then run 
`./gradlew generatePostgresJooqSchemaSource`

To run tests: `./gradlew build`

To build web: `./gradlew webpack`.

To create production-ready builds: `./gradlew build 
webpackMin installDist`. UI files are output in `ui/build/web` and 
service files are output in `service/build/install/service`.

## Environment

In order to run the service, you must have a `GOOGLE_SIGNIN_CLIENT_ID` 
variable set to the same value as used in the UI.

## TODO
* UI side graceful error handling, alert messages
* Add missing indexes to response table
* Add page for poll creator to remove votes
* Add script or gradle plugin to automatically dockerize
* Add option to show poll creators email
* Dark mode
