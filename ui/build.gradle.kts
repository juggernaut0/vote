import com.moowork.gradle.node.npm.NpmTask
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinJsDce

plugins {
    id("kotlin2js")
    id("kotlin-dce-js")
    id("com.moowork.node")
}

repositories {
    mavenCentral()
    maven("https://kotlin.bintray.com/kotlinx")
    maven("https://juggernaut0.github.io/m2/repository")
    mavenLocal()
}

dependencies {
    compile(project(":"))

    compile(kotlin("stdlib-js"))
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.1.1")

    compile("com.github.juggernaut0.kui:kui:0.3.0")
}

tasks.withType<Kotlin2JsCompile>().forEach {
    it.kotlinOptions.moduleKind = "umd"
}

tasks.getByName<KotlinJsDce>("runDceKotlinJs").keep("kotlin.defineModule")

tasks {
    val copyStaticWeb by registering(Copy::class) {
        group = "build"
        from("web")
        into("$buildDir/web")
    }
    
    val populateNodeModules by registering(Copy::class) {
        dependsOn("runDceKotlinJs")

        from(getByName<KotlinJsDce>("runDceKotlinJs").destinationDir)
        include("*.js")
        into("$buildDir/node_modules")
    }

    val webpack by registering(NpmTask::class) {
        dependsOn(populateNodeModules)
        dependsOn(copyStaticWeb)
        setArgs(listOf("run", "webpack"))
    }

    val webpackMin by registering(NpmTask::class) {
        dependsOn(populateNodeModules)
        dependsOn(copyStaticWeb)
        setArgs(listOf("run", "webpackMin"))
    }
}
