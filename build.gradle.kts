import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.cjcrafter"
version = "3.0.5"

plugins {
    kotlin("jvm") version "1.9.21"
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
}

// See https://github.com/Minecrell/plugin-yml
bukkit {
    main = "com.cjcrafter.armormechanics.ArmorMechanics"
    apiVersion = "1.13"

    authors = listOf("CJCrafter")
    depend = listOf("MechanicsCore")
    softDepend = listOf("WeaponMechanics")
}

repositories {
    mavenCentral()

    maven(url = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/") // Spigot
    maven(url = "https://mvn.lumine.io/repository/maven-public/") // MythicMobs
    maven(url = "https://repo.jeff-media.com/public/") // SpigotUpdateChecker
}

dependencies {
    implementation("org.bstats:bstats-bukkit:3.0.1")
    implementation("com.jeff_media:SpigotUpdateChecker:3.0.3")

    compileOnly("org.spigotmc:spigot-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("io.lumine:Mythic-Dist:5.3.5")
    compileOnly("com.cjcrafter:mechanicscore:3.4.1")
    compileOnly("com.cjcrafter:weaponmechanics:3.4.1")
}

tasks.shadowJar {
    archiveFileName.set("ArmorMechanics-${project.version}.jar")

    dependencies {
        relocate ("org.bstats", "com.cjcrafter.armormechanics.lib.bstats") {
            include(dependency("org.bstats:"))
        }
        relocate("com.jeff_media", "com.cjcrafter.armormechanics.lib") {
            include(dependency("com.jeff_media:"))
        }
        relocate ("kotlin.", "com.cjcrafter.armormechanics.lib.kotlin.") {
            include(dependency("org.jetbrains.kotlin:"))
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
        options.release.set(16)
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "16"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "16"
}