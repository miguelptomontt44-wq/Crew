plugins {
    java
}

group = "dev.crews"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    // Si sale una build mas nueva de 26.3, cambia solo este numero.
    compileOnly("io.papermc.paper:paper-api:26.3.build.143-beta")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.withType<JavaCompile> {
    options.release.set(25)
    options.encoding = "UTF-8"
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand("version" to project.version)
    }
}

tasks.jar {
    archiveFileName.set("Crews-${project.version}.jar")
}
