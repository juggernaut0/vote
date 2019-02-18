plugins {
    kotlin("jvm")
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("org.postgresql:postgresql:42.2.5")
    implementation("org.flywaydb:flyway-core:5.2.4")
}

application {
    mainClassName = "vote.db.MigrateKt"
}
