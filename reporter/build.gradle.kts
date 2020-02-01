import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.3.60"

    id("com.github.johnrengelman.shadow") version "5.2.0"
}

repositories {
    mavenLocal()
    jcenter()
    maven("https://jitpack.io")
}

version = "0.1"

dependencies {
    val version = object {
        val moshi = "1.9.2"
    }

    implementation(kotlin("stdlib-jdk8"))

    implementation("com.squareup.moshi:moshi:${version.moshi}")
    implementation("com.squareup.moshi:moshi-kotlin:${version.moshi}")

    implementation("org.junit.jupiter:junit-jupiter:5.5.2")
    implementation("org.junit.platform:junit-platform-launcher:1.6.0")

    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.3.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<ShadowJar> {
    archiveFileName.set("reporter.${project.version}.jar")
}
