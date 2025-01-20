import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.cjcrafter"
version = "3.1.0"

plugins {
    `java-library`
    kotlin("jvm") version "1.9.21"
    `maven-publish`
    id("com.gradleup.shadow") version "8.3.3"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
}

// See https://github.com/Minecrell/plugin-yml
bukkit {
    main = "com.cjcrafter.armormechanics.ArmorMechanics"
    apiVersion = "1.13"
    foliaSupported = true

    authors = listOf("CJCrafter")
    depend = listOf("MechanicsCore")
    softDepend = listOf("WeaponMechanics")
}

repositories {
    mavenLocal()
    mavenCentral()

    maven(url = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/") // Spigot
    maven(url = "https://mvn.lumine.io/repository/maven-public/") // MythicMobs
    maven(url = "https://repo.jeff-media.com/public/") // SpigotUpdateChecker
}

dependencies {
    implementation("org.bstats:bstats-bukkit:3.0.1")
    implementation("com.jeff_media:SpigotUpdateChecker:3.0.4")

    compileOnly("org.spigotmc:spigot-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("io.lumine:Mythic-Dist:5.7.2")
    compileOnly("com.cjcrafter:foliascheduler:0.6.3")
    compileOnly("com.cjcrafter:mechanicscore:4.0.2")
    compileOnly("com.cjcrafter:weaponmechanics:4.0.4")
    compileOnly("net.kyori:adventure-api:4.18.0")
    compileOnly("dev.jorel:commandapi-bukkit-core:9.7.0")
    compileOnly("dev.jorel:commandapi-bukkit-kotlin:9.7.0")
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
    }

    relocate("kotlin.", "me.deecaad.core.lib.kotlin.")
    relocate("com.cjcrafter.foliascheduler", "me.deecaad.core.lib.scheduler")
    relocate("dev.jorel.commandapi", "me.deecaad.core.lib.commandapi")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
        options.release.set(21)
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
    jvmTarget = "21"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "21"
}