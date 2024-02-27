plugins {
    kotlin("plugin.serialization")
    id("dev.twarner.kotlin-web")
}

dependencies {
    jsMainImplementation(projects.common)
    jsMainImplementation(libs.kui)
    jsMainImplementation(libs.twarner.auth.ui)
}

kotlin {
    js {
        sourceSets.all {
            languageSettings {
                optIn("kotlin.RequiresOptIn")
            }
        }
    }
}
