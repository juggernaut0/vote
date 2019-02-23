# SoVote

One-time setup: `./gradlew npmInstall`

To generate jooq classes, first start DB & migrate with 
`./start_db.sh`. Then run `./gradlew generatePostgresJooqSchemaSource`

To run tests: `./gradlew build`

To create web: `./gradlew webpack`. `./start_web.sh` will do this 
automatically, as well as start nginx in docker

To create production-ready builds: `./gradlew clean build 
webpackMin installDist`. UI files are output in `ui/build/web` and 
service files are output in `service/build/install/service`.

## TODO
* Prepopulate vote page with previous response if any
* Add script or gradle plugin to dockerize
* Document env variables required
* Change styling of results page to be fancier than unordered lists
* Dark mode
* UI side graceful error handling
