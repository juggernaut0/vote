import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    java
    application
    id("nu.studer.jooq").version("3.0.3")
}

repositories {
    mavenCentral()
    maven("https://kotlin.bintray.com/kotlinx")
}

dependencies {
    implementation(project(":"))

    implementation(kotlin("stdlib-jdk8"))
    
    implementation("io.ktor:ktor-server-core:1.1.2")
    implementation("io.ktor:ktor-server-jetty:1.1.2")

    implementation("com.google.inject:guice:4.2.2:no_aop")

    implementation("ch.qos.logback:logback-classic:1.2.3")

    implementation("org.postgresql:postgresql:42.2.5")
    implementation("org.flywaydb:flyway-core:5.2.4")
    implementation("org.jooq:jooq:3.11.9")
    jooqRuntime("org.postgresql:postgresql:42.2.5")
    implementation("com.zaxxer:HikariCP:3.2.0")

    testImplementation(kotlin("test-junit"))
}

application {
    mainClassName = "vote.MainKt"
}

tasks.withType<KotlinCompile>().forEach {
    it.kotlinOptions.jvmTarget = "1.8"
    it.dependsOn("generatePostgresJooqSchemaSource")
}

apply {
    from("jooq.gradle")
}
