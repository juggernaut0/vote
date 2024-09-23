pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        maven("https://juggernaut0.github.io/m2/repository")
    }

    plugins {
        kotlin("jvm") version "1.9.25"
        kotlin("plugin.serialization") version "1.9.25"
    }
}

plugins {
    id("dev.twarner.settings") version "1.0.4"
}

include("dbmigrate", "common", "service", "web")
rootProject.name = "vote"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
