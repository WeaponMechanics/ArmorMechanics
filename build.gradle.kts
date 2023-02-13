import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

group = "me.cjcrafter"
version = "2.0.1"

plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.0"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2"
}

configurations {
    compileClasspath.get().extendsFrom(create("shadeOnly"))
}

// See https://github.com/Minecrell/plugin-yml
bukkit {
    main = "me.cjcrafter.armormechanics.ArmorMechanics"
    apiVersion = "1.13"

    authors = listOf("CJCrafter")
    softDepend = listOf("MechanicsCore", "WeaponMechanics")
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
            username = findProperty("user").toString()
            password = findProperty("pass").toString() // Check WeaponMechanics wiki on how to use this in your repo!
        }
    }

    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/WeaponMechanics/MechanicsAutoDownload")
        credentials {
            username = findProperty("user").toString()
            password = findProperty("pass").toString() // Check WeaponMechanics wiki on how to use this in your repo!
        }
    }

    maven {
        name = "lumine-repo"
        url = uri("http://mvn.lumine.io/repository/maven-public/")
        isAllowInsecureProtocol = true
    }
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.19.3-R0.1-SNAPSHOT")
    implementation("io.lumine:Mythic-Dist:5.0.1-SNAPSHOT")
    compileOnly("me.deecaad:mechanicscore:2.0.1")
    compileOnly("me.deecaad:weaponmechanics:2.0.1")
    implementation("org.bstats:bstats-bukkit:3.0.0")
    implementation("me.cjcrafter:mechanicsautodownload:1.1.2")
}

tasks.named<ShadowJar>("shadowJar") {
    classifier = null
    archiveFileName.set("ArmorMechanics-${project.version}.jar")
    configurations = listOf(project.configurations["shadeOnly"], project.configurations["runtimeClasspath"])

    dependencies {
        relocate ("org.bstats", "me.cjcrafter.armormechanics.lib.bstats") {
            include(dependency("org.bstats:"))
        }
        relocate ("me.cjcrafter.auto", "me.cjcrafter.armormechanics.lib.auto") {
            include(dependency("me.cjcrafter:mechanicsautodownload"))
        }
    }
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
        options.release.set(16)
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
}
