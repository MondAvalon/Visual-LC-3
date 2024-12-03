plugins {
    kotlin("jvm") version "2.0.0"
    application
}

application {
    mainClass = "Main"
    applicationDefaultJvmArgs = listOf("-Dsun.java2d.opengl=true")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}