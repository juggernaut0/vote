import com.moowork.gradle.node.task.NodeTask
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-serialization:1.3.50")
    }
}

plugins {
    kotlin("multiplatform").version("1.3.50")
    id("com.moowork.node").version("1.2.0")
}

apply(plugin = "kotlinx-serialization")

repositories {
    mavenLocal()
    jcenter()
    maven("https://kotlin.bintray.com/kotlinx")
    maven("https://juggernaut0.github.io/m2/repository")
}

kotlin {
    jvm()
    js()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.github.juggernaut0:multiplatform-utils-metadata:0.1.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:0.11.1")
            }
        }

        val jvmMain by getting {
            dependencies {
                api("com.github.juggernaut0:multiplatform-utils-jvm:0.1.0")
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.11.1")
            }
        }

        val jsMain by getting {
            dependencies {
                api("com.github.juggernaut0:multiplatform-utils-js:0.1.0")
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:0.11.1")
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}

tasks.withType<Kotlin2JsCompile>().forEach {
    it.kotlinOptions.moduleKind = "umd"
}

tasks {
    val populateNodeModules by registering(Copy::class) {
        dependsOn("compileKotlinJs")

        from(getByName<Kotlin2JsCompile>("compileKotlinJs").destinationDir)

        configurations["jsTestCompileClasspath"].forEach {
            from(zipTree(it.absolutePath).matching { include("*.js") })
        }

        into("$buildDir/node_modules")
    }

    val runJest by registering(NodeTask::class) {
        dependsOn("jsTestClasses")
        dependsOn(populateNodeModules)
        setScript(file("node_modules/jest/bin/jest.js"))
    }

    getByName("jsTest").dependsOn(runJest)
}
