# SoVote

One-time setup: `./gradlew npmInstall`

To run tests: `./gradlew build`

To create web: `./gradlew webpack`. `./start_web.sh` will do this 
automatically, as well as start nginx in docker

To create production-ready builds: `./gradlew clean build 
webpackMin installDist`. UI files are output in `ui/build/web` and 
service files are output in `service/build/install/service`.
