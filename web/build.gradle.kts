plugins {
    kotlin("js")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(project(":common"))
    implementation(kotlin("stdlib-js"))

    implementation("com.github.juggernaut0.kui:kui:0.12.0")

    implementation("dev.twarner.auth:auth-ui:8")
}

kotlin {
    js {
        browser {
            testTask {
                useKarma {
                    useFirefoxHeadless()
                }
            }
        }
        compilations.all {
            kotlinOptions {
                moduleKind = "commonjs"
                sourceMap = true
                sourceMapEmbedSources = "always"
            }
        }
        sourceSets.all {
            languageSettings {
                optIn("kotlin.RequiresOptIn")
            }
        }
        binaries.executable()
    }
}
