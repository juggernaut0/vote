import com.moowork.gradle.node.task.NodeTask
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

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
    id("com.moowork.node").version("1.2.0")
}

apply(plugin = "kotlinx-serialization")

repositories {
    jcenter()
    maven { url = uri("https://kotlin.bintray.com/kotlinx") }
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
