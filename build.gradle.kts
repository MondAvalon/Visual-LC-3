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
    testImplementation("com.squareup.okhttp3:okhttp:4.12.0")
    testImplementation("org.json:json:20240303")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}