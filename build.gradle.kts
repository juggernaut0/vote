import java.net.URI

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-serialization:1.3.21")
    }
}

plugins {
    kotlin("multiplatform").version("1.3.21")
}

apply(plugin = "kotlinx-serialization")

repositories {
    jcenter()
    maven { url = URI("https://kotlin.bintray.com/kotlinx") }
}

kotlin {
    jvm()
    js()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:0.10.0")
            }
        }

        val jvmMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.10.0")
            }
        }

        val jsMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:0.10.0")
            }
        }
    }
}
