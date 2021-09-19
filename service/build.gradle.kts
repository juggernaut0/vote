import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.Dockerfile

plugins {
    kotlin("jvm")
    java
    application
    id("nu.studer.jooq")
    id("com.bmuschko.docker-remote-api")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":dbmigrate"))

    implementation(kotlin("stdlib-jdk8"))
    
    val ktorVersion = "1.6.3"
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-jetty:$ktorVersion")
    implementation("io.ktor:ktor-client-apache:$ktorVersion")

    implementation("com.google.inject:guice:5.0.1")

    implementation("ch.qos.logback:logback-classic:1.2.6")

    implementation("org.postgresql:postgresql:42.2.23")
    jooqGenerator("org.postgresql:postgresql:42.2.5")
    implementation("com.zaxxer:HikariCP:5.0.0")

    implementation("dev.twarner.auth:auth-common:7")

    implementation("io.github.config4k:config4k:0.4.2")

    testImplementation(kotlin("test-junit"))
}

kotlin {
    target {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }
}

application {
    mainClass.set("vote.MainKt")
}

jooq {
    configurations {
        create("main") {
            version.set("3.15.2")
            generateSchemaSourceOnCompilation.set(true)
            jooqConfiguration.apply {
                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = "jdbc:postgresql://localhost:6432/vote"
                    user = "vote"
                    password = "vote"
                }
                generator.apply {
                    name = "org.jooq.codegen.DefaultGenerator"
                    strategy.apply {
                        name = "org.jooq.codegen.DefaultGeneratorStrategy"
                    }
                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = "public"
                        includes = ".*"
                        excludes = "flyway_schema_history"
                    }
                    generate.apply {
                        isRelations = true
                        isDeprecated = false
                        isRecords = true
                        isFluentSetters = false
                    }
                    target.apply {
                        packageName = "vote.db.jooq"
                        directory = "build/generated/source/jooq/main"
                    }
                }
            }
        }
    }
}

tasks {
    val copyWeb by registering(Copy::class) {
        if (version.toString().endsWith("SNAPSHOT")) {
            dependsOn(":web:browserDevelopmentWebpack")
        } else {
            dependsOn(":web:browserProductionWebpack")
        }
        group = "build"
        from(project(":web").buildDir.resolve("distributions"))
        into(processResources.map { it.destinationDir.resolve("static") })
    }

    classes {
        dependsOn(copyWeb)
    }

    (run) {
        environment(projectDir.resolve("local.env").readLines().associate { line -> line.split('=', limit = 2).let { it[0] to it[1] } })
    }

    val copyDist by registering(Copy::class) {
        dependsOn(distTar)
        from(distTar.flatMap { it.archiveFile })
        into("$buildDir/docker")
    }

    val dockerfile by registering(Dockerfile::class) {
        dependsOn(copyDist)

        from("openjdk:11-jre-slim")
        addFile(distTar.flatMap { it.archiveFileName }.map { Dockerfile.File(it, "/app/") })
        defaultCommand(distTar.flatMap { it.archiveFile }.map { it.asFile.nameWithoutExtension }.map { listOf("/app/$it/bin/${project.name}") })
    }

    val dockerBuild by registering(DockerBuildImage::class) {
        dependsOn(dockerfile)

        if (version.toString().endsWith("SNAPSHOT")) {
            images.add("${rootProject.name}:SNAPSHOT")
        } else {
            images.add("juggernaut0/${rootProject.name}:$version")
        }
    }
}
