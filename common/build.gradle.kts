plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvm()
    js {
        browser {
            compilations.all {
                kotlinOptions {
                    moduleKind = "commonjs"
                    sourceMap = true
                    sourceMapEmbedSources = "always"
                }
            }
        }
    }

    sourceSets {
        val multiplatformUtilsVersion = "0.6.4"

        commonMain {
            dependencies {
                implementation("com.github.juggernaut0:multiplatform-utils:$multiplatformUtilsVersion")
            }
        }

        named("jvmMain") {
            dependencies {
                api("com.github.juggernaut0:multiplatform-utils-ktor-jvm:$multiplatformUtilsVersion")
            }
        }
    }
}
