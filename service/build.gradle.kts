import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("dev.twarner.kotlin-service")
    kotlin("kapt")
}

dependencies {
    implementation(projects.common)
    implementation(projects.dbmigrate)
    webResource(projects.web)

    implementation(libs.javalin)
    implementation(libs.multiplatformUtils.javalin)
    implementation(libs.twarner.auth.plugins.javalin)

    implementation(libs.dagger)
    kapt(libs.dagger.compiler)

    implementation(libs.logback)
    implementation(libs.hikari)
    implementation(libs.config4k)

    testImplementation(kotlin("test"))
}

application {
    mainClass.set("vote.MainKt")
}

tasks {
    (run) {
        environment(projectDir.resolve("local.env").readLines().associate { line -> line.split('=', limit = 2).let { it[0] to it[1] } })
    }

    withType<JavaCompile>().configureEach {
        options.release = 21
    }

    withType<KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = "21"
    }
}
