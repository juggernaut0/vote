# SoVote

One-time setup: `./gradlew npmInstall`

To generate jooq classes, first start DB & migrate with 
`./start_db.sh`. Then run `./gradlew generatePostgresJooqSchemaSource`

To run tests: `./gradlew build`

To create web: `./gradlew webpack`. `./start_web.sh` will do this 
automatically, as well as start nginx in docker

To create production-ready builds: `./gradlew build 
webpackMin installDist`. UI files are output in `ui/build/web` and 
service files are output in `service/build/install/service`.

## Environment

In order to run the service, you must have a `GOOGLE_SIGNIN_CLIENT_ID` 
variable set to the same value as used in the UI.

## TODO
* Improve results page
  * Freeform: Remove empty strings, group identical responses
  * Votes: add background bars (colored?) to represent percentages
  * Use websockets or polling to automatically update
  * Show number of responses counted per question
* UI side graceful error handling
* Add page for poll creator to remove votes
* Add script or gradle plugin to dockerize
* Add option to show poll creators email
* Dark mode
