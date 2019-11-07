import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    java
    application
    id("nu.studer.jooq").version("3.0.3")
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://kotlin.bintray.com/kotlinx")
    maven("https://juggernaut0.github.io/m2/repository")
}

dependencies {
    implementation(project(":"))

    implementation(kotlin("stdlib-jdk8"))
    
    val ktorVersion = "1.2.5"
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-jetty:$ktorVersion")
    implementation("io.ktor:ktor-auth:$ktorVersion")

    implementation("com.google.inject:guice:4.2.2:no_aop")

    implementation("ch.qos.logback:logback-classic:1.2.3")

    implementation("org.postgresql:postgresql:42.2.5")
    implementation("org.flywaydb:flyway-core:5.2.4")
    implementation("org.jooq:jooq:3.11.9")
    jooqRuntime("org.postgresql:postgresql:42.2.5")
    implementation("com.zaxxer:HikariCP:3.2.0")

    implementation("com.google.api-client:google-api-client:1.28.0")

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
