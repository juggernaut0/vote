pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        maven("https://juggernaut0.github.io/m2/repository")
    }

    plugins {
        kotlin("jvm") version "2.0.21"
        kotlin("plugin.serialization") version "2.0.21"
    }
}

plugins {
    id("dev.twarner.settings") version "1.0.3"
}

include("dbmigrate", "common", "service", "web")
rootProject.name = "vote"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
