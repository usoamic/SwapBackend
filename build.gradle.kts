import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.3.72"
    kotlin("kapt") version "1.3.72"
}

allprojects {
    group = "io.usoamic"
    version = "1.2.5"
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

repositories {
    mavenCentral()
    jcenter()
    maven("https://jitpack.io")
}

dependencies {
    compile("org.jetbrains.kotlin", "kotlin-stdlib", "1.3.72")
    compile("org.web3j", "core", "4.6.0")
    compile("org.jetbrains.exposed", "exposed-core", "0.28.1")
    compile("org.jetbrains.exposed", "exposed-dao", "0.28.1")
    compile("org.jetbrains.exposed", "exposed-jdbc", "0.28.1")
    compile("mysql", "mysql-connector-java", "8.0.22")
    compile("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")
    compile("com.google.code.gson", "gson", "2.8.6")
    compile("com.github.usoamic", "usoamickt", "v1.2.1")
    compile("org.telegram", "telegrambots", "4.9")
}

val fatJar = task("fatJar", type = Jar::class) {
    baseName = "${project.name}-fat"
    // manifest Main-Class attribute is optional.
    // (Used only to provide default main class for executable jar)
    manifest {
        attributes["Main-Class"] = "io.usoamic.swapbackend.App"
    }
    from(configurations.runtime.get().map { if (it.isDirectory) it else zipTree(it) })
    exclude(
        "META-INF/*.SF",
        "META-INF/*.DSA",
        "META-INF/*.RSA",
        "junit",
        "org.mockito",
        "org.hamcrest"
    )
    with(tasks["jar"] as CopySpec)
}

tasks {
    "build" {
        dependsOn(fatJar)
    }
}