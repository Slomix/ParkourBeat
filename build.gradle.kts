plugins {
    `java`
    id("com.diffplug.spotless") version "6.25.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("xyz.jpenilla.run-paper") version "2.2.2"
}

group = "me.bomb.parkourbeat"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public")
    maven("https://oss.sonatype.org/content/groups/public")
    maven("https://repo.infernalsuite.com/repository/maven-snapshots")
    maven("https://repo.glaremasters.me/repository/concuncan")
    maven("https://repo.panda-lang.org/releases")
}

dependencies {
    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("com.grinderwolf:slimeworldmanager-api:2.2.1")
    compileOnly(files("run/plugins/AMusic_v0.12.jar"))

    implementation("org.jetbrains:annotations:24.1.0")

    annotationProcessor("org.projectlombok:lombok:1.18.30")
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_16
}

tasks {
    withType<JavaCompile> {
        options.compilerArgs.add("-parameters")
    }
    runServer {
        minecraftVersion("1.16.5")
    }
}