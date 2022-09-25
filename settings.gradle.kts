include("dbmigrate", "common", "service", "web")
rootProject.name = "vote"

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://juggernaut0.github.io/m2/repository")
    }
}

pluginManagement {
    plugins {
        id("nu.studer.jooq") version "7.1.1"
        id("com.bmuschko.docker-remote-api") version "7.1.0"
    }
}
