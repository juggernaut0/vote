plugins {
    kotlin("jvm")
    application
}

repositories {
    mavenCentral()
    maven("https://kotlin.bintray.com/kotlinx")
}

dependencies {
    implementation(project(":"))

    implementation(kotlin("stdlib-jdk8"))
}

application {
    mainClassName = "vote.MainKt"
}
