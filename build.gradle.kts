import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

group = "me.cjcrafter"
version = "1.0.0"

plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.0"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.1"
}

configurations {
    compileClasspath.get().extendsFrom(create("shadeOnly"))
}

// See https://github.com/Minecrell/plugin-yml
bukkit {
    main = "me.cjcrafter.armormechanics.ArmorMechanics"
    apiVersion = "1.13"

    authors = listOf("CJCrafter")
    softDepend = listOf("MechanicsCore")
}

repositories {
    mavenCentral()

    maven {
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }

    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/WeaponMechanics/MechanicsMain")
        credentials {
            username = "CJCrafter"
            password = "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" // this is a public token
        }
    }
}

dependencies {
    api("org.spigotmc:spigot-api:1.18.2-R0.1-SNAPSHOT")
    compileOnly("me.deecaad:mechanicscore:+")
    compileOnly("me.deecaad:weaponmechanics:+")
}

tasks.named<ShadowJar>("shadowJar") {
    archiveFileName.set("ArmorMechanics-${project.version}.jar")
    configurations = listOf(project.configurations["shadeOnly"], project.configurations["runtimeClasspath"])
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
        options.release.set(8)
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
}