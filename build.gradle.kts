plugins {
    kotlin("jvm") version "2.0.0"
    application
}

application {
    mainClass = "Main"
    applicationDefaultJvmArgs = listOf("-Dsun.java2d.opengl=true")
}

repositories {
    maven("https://maven.aliyun.com/repository/public")
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

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = application.mainClass.get() // 设置 Main-Class 属性
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    // 包含运行时依赖
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}