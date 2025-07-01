// build.gradle.kts

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.yourpackage"
version = "1.3.0-SNAPSHOT" // Version reflects the addition of Tier 2 & 3 features

repositories {
    mavenCentral()
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    // Repository for the Vault API
    maven { url = uri("https://jitpack.io") }
    // Repository for PlaceholderAPI
    maven { url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/") }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    // The Vault API dependency
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    // The PlaceholderAPI dependency
    compileOnly("me.clip:placeholderapi:2.11.6")
}

java {
    // Target Java 21, which is recommended for Minecraft 1.21+
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    shadowJar {
        archiveBaseName.set("MysteryMerchant")
        archiveClassifier.set("")
        archiveVersion.set(project.version.toString())
    }

    build {
        dependsOn(shadowJar)
    }

    processResources {
        filesMatching("plugin.yml") {
            expand(mapOf("version" to project.version))
        }
    }
}
