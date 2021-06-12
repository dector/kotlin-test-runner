plugins {
    kotlin("jvm") version "1.3.60"
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("org.junit.jupiter:junit-jupiter:5.5.2")
}

repositories {
    mavenCentral()
}

tasks.create("resolveDependencies") {
    doLast {
        project.rootProject.allprojects.forEach { p ->
            p.buildscript.configurations
                .filter { it.isCanBeResolved }
                .forEach { it.resolve() }

            p.configurations
                .filter { it.isCanBeResolved }
                .forEach { it.resolve() }
        }
    }
}
