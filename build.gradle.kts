import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jreleaser.model.Active
import xyz.jpenilla.resourcefactory.bukkit.Permission

group = "com.cjcrafter"
version = "4.1.1-SNAPSHOT"

plugins {
    `java-library`
    kotlin("jvm") version "2.2.0"
    id("com.gradleup.shadow") version "8.3.5"
    id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.3.0"
    `maven-publish`
    id("org.jreleaser") version "1.18.0"
}

bukkitPluginYaml {
    main = "com.cjcrafter.armormechanics.ArmorMechanics"
    apiVersion = "1.13"
    foliaSupported = true

    authors = listOf("CJCrafter")
    depend = listOf("MechanicsCore")
    softDepend = listOf("WeaponMechanics")

    permissions {
        register("armormechanics.preventremovebypass") {
            description = "Allow users to remove armor which normally can't be removed"
            default = Permission.Default.OP
        }
    }
}

repositories {
    mavenCentral()
    maven(url = "https://central.sonatype.com/repository/maven-snapshots/") // MechanicsCore Snapshots
    maven(url = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/") // Spigot
    maven(url = "https://mvn.lumine.io/repository/maven-public/") // MythicMobs
    maven(url = "https://repo.jeff-media.com/public/") // SpigotUpdateChecker
}

dependencies {
    // Core Minecraft dependencies
    compileOnly(libs.spigotApi)
    compileOnly(libs.mechanicsCore)
    compileOnly(libs.weaponMechanics)

    // External "hooks" or plugins that we might interact with
    compileOnly(libs.mythicMobs)

    // Shaded dependencies
    compileOnly(libs.adventureApi)
    compileOnly(libs.bstats)
    compileOnly(libs.commandApi)
    compileOnly(libs.commandApiKotlin)
    compileOnly(libs.foliaScheduler)
    compileOnly(libs.spigotUpdateChecker)
}

tasks.shadowJar {
    archiveFileName.set("ArmorMechanics-${project.version}.jar")

    // the kotlin plugin adds kotlin-stdlib to the classpath, but we don't want it in the shadow jar
    exclude("kotlin/**")
    exclude("META-INF/*.kotlin_module")
    exclude("org/jetbrains/annotations/**")
    exclude("META-INF/annotations/**")

    val libPackage = "me.deecaad.core.lib"

    relocate("org.bstats", "$libPackage.bstats")
    relocate("net.kyori", "$libPackage.kyori")
    relocate("com.jeff_media.updatechecker", "$libPackage.updatechecker")
    relocate("dev.jorel.commandapi", "$libPackage.commandapi")
    relocate("com.cjcrafter.foliascheduler", "$libPackage.scheduler")
    relocate("kotlin.", "$libPackage.kotlin.")
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

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks.named("javadoc").map { it.outputs.files })
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(sourcesJar)
            artifact(javadocJar)

            groupId = "com.cjcrafter"
            artifactId = "armormechanics"
            version = project.version.toString()

            pom {
                name.set("ArmorMechanics")
                description.set("Lightweight custom armor Bukkit plugin ")
                url.set("https://github.com/WeaponMechanics/ArmorMechanics")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("CJCrafter")
                        name.set("Collin Barber")
                        email.set("collinjbarber@gmail.com")
                    }
                    developer {
                        id.set("DeeCaaD")
                        name.set("DeeCaaD")
                        email.set("perttu.kangas@hotmail.fi")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/WeaponMechanics/ArmorMechanics.git")
                    developerConnection.set("scm:git:ssh://github.com/WeaponMechanics/ArmorMechanics.git")
                    url.set("https://github.com/WeaponMechanics/ArmorMechanics")
                }
            }
        }
    }

    // Deploy this repository locally for staging, then let the root project actually
    // upload the maven repo using jReleaser
    repositories {
        maven {
            name = "stagingDeploy"
            url = layout.buildDirectory.dir("staging-deploy").map { it.asFile.toURI() }.get()
        }
    }
}

jreleaser {
    gitRootSearch.set(true)

    project {
        name.set("ArmorMechanics")
        group = "com.cjcrafter"
        version = findProperty("version").toString()
        description = "Lightweight custom armor Bukkit plugin "
        authors.add("CJCrafter <collinjbarber@gmail.com>")
        authors.add("DeeCaaD <perttu.kangas@hotmail.fi>")
        license = "MIT" // SPDX identifier
        copyright = "Copyright © 2023-2025 CJCrafter, DeeCaaD"

        links {
            homepage.set("https://github.com/WeaponMechanics/ArmorMechanics")
            documentation.set("https://github.com/WeaponMechanics/ArmorMechanics#readme")
        }

        java {
            groupId = "com.cjcrafter"
            artifactId = "armormechanics"
            version = findProperty("version").toString()
        }

        snapshot {
            fullChangelog.set(true)
        }
    }

    signing {
        active.set(Active.ALWAYS)
        armored.set(true)
    }

    deploy {
        maven {
            mavenCentral {
                create("releaseDeploy") {
                    active.set(Active.RELEASE)
                    url.set("https://central.sonatype.com/api/v1/publisher")
                    // run ./gradlew publish before deployment
                    stagingRepository("build/staging-deploy")
                    // Credentials (JRELEASER_MAVENCENTRAL_USERNAME, JRELEASER_MAVENCENTRAL_PASSWORD or JRELEASER_MAVENCENTRAL_TOKEN)
                    // will be picked up from ~/.jreleaser/config.toml
                }
            }

            nexus2 {
                create("sonatypeSnapshots") {
                    active.set(Active.SNAPSHOT)
                    url.set("https://central.sonatype.com/repository/maven-snapshots/")
                    snapshotUrl.set("https://central.sonatype.com/repository/maven-snapshots/")
                    applyMavenCentralRules = true
                    snapshotSupported = true
                    closeRepository = true
                    releaseRepository = true
                    stagingRepository("build/staging-deploy")
                }
            }
        }
    }

    distributions {
        create("armormechanics") {
            active.set(Active.ALWAYS)
            distributionType.set(org.jreleaser.model.Distribution.DistributionType.SINGLE_JAR)
            artifact {
                path.set(file("build/libs/ArmorMechanics-${findProperty("version")}.jar"))
            }
        }
    }

    release {
        github {
            repoOwner.set("WeaponMechanics")
            name.set("ArmorMechanics")
            host.set("github.com")

            val version = findProperty("version").toString()
            val isSnapshot = version.endsWith("-SNAPSHOT")
            releaseName.set(if (isSnapshot) "SNAPSHOT" else "v$version")
            tagName.set("v{{projectVersion}}")
            draft.set(false)
            skipTag.set(isSnapshot)
            overwrite.set(false)
            update { enabled.set(isSnapshot) }

            prerelease {
                enabled.set(isSnapshot)
                pattern.set(".*-SNAPSHOT")
            }

            commitAuthor {
                name.set("Collin Barber")
                email.set("collinjbarber@gmail.com")
            }

            changelog {
                formatted.set(Active.ALWAYS)
                preset.set("conventional-commits")
                format.set("- {{commitShortHash}} {{commitTitle}}")
                contributors {
                    enabled.set(true)
                    format.set("{{contributorUsernameAsLink}}")
                }
                hide {
                    contributors.set(listOf("[bot]"))
                }
            }
        }
    }
}
