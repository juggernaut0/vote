plugins {
    id("kotlin2js")
}

repositories {
    mavenCentral()
    maven("https://kotlin.bintray.com/kotlinx")
    mavenLocal()
}

dependencies {
    compile(project(":"))

    compile(kotlin("stdlib-js"))

    compile("com.github.juggernaut0.kui:kui:0.2.1")
}

tasks {
    val assembleWeb by registering(Copy::class) {
        dependsOn(classes)
        group = "build"
        val outputDir = file("$buildDir/web/js")
        inputs.property("compileClasspath", configurations.compileClasspath.get())
        outputs.dir(outputDir)

        includeEmptyDirs = false
        configurations.compileClasspath.get().forEach {
            from(zipTree(it.absolutePath)) {
                includeEmptyDirs = false
                include { f -> f.path.endsWith(".js") }
            }
        }
        from(sourceSets.main.get().output) {
            exclude("**/*.kjsm")
        }
        include("**/*.js")
        exclude("META-INF/**")
        into(outputDir)
    }

    val copyStaticWeb by registering(Copy::class) {
        group = "build"
        from("web")
        into("$buildDir/web")
    }

    assemble {
        dependsOn(assembleWeb)
        dependsOn(copyStaticWeb)
    }
}
