import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    kotlin("jvm") version "1.3.60"
}

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))

    testImplementation(files("libs/reporter.0.1.jar"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.5.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
        events("passed", "failed", "skipped")
    }
}
