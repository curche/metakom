import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    application
}

group = "xyz.curche"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val okhttpversion = "4.9.3"
    implementation("com.squareup.okhttp3:okhttp:$okhttpversion")
    implementation("com.squareup.okhttp3:logging-interceptor:$okhttpversion")
    implementation("com.squareup.okhttp3:okhttp-dnsoverhttps:$okhttpversion")

    val kotlinJsonVersion = "1.3.1"
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinJsonVersion")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "MainKt"
    }
    configurations["compileClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

application {
    mainClass.set("MainKt")
}