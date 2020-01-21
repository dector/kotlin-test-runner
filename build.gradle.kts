import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    kotlin("jvm") version "1.3.60"

    id("org.jetbrains.dokka") version "0.10.0"

    id("com.github.johnrengelman.shadow") version "5.2.0"
    application
}

repositories {
    mavenLocal()
    jcenter()
    maven("https://jitpack.io")
}

dependencies {
    val version = object {
        val moshi = "1.9.2"
    }

    implementation(kotlin("stdlib-jdk8"))

    implementation("com.xenomachina:kotlin-argparser:2.0.7")

    implementation("com.squareup.moshi:moshi:${version.moshi}")
    implementation("com.squareup.moshi:moshi-kotlin:${version.moshi}")

    implementation("org.dom4j:dom4j:2.1.1")

    implementation("org.junit.platform:junit-platform-launcher:1.2.0")
    implementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    implementation("org.junit.jupiter:junit-jupiter-engine:5.6.0")

    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.3.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

application {
    mainClassName = "exercism.kotlin.autotests.runner.MainKt"
}

tasks.withType<ShadowJar> {
    archiveFileName.set("autotest-runner.jar")
}

tasks.withType<DokkaTask> {
    outputFormat = "gfm"
    outputDirectory = "docs"

    configuration {
        includes = listOf("docs/dokka-includes/modules.md")
    }
}
