plugins {
    kotlin("jvm")
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("org.postgresql:postgresql:42.3.1")
    implementation("org.flywaydb:flyway-core:9.22.3")
}

application {
    mainClass.set("vote.db.MigrateKt")
}

tasks {
    (run) {
        args = listOf("postgres://vote:vote@localhost:6432/vote")
    }
}
