val javaVersion = 17
val silkVersion = "1.10.3"

plugins {
    kotlin("jvm") version "1.9.21"
    id("fabric-loom") version "1.5-SNAPSHOT"
    kotlin("plugin.serialization") version "1.9.21"
}

val mcVersion = "1.20.4"
group = "gg.norisk"
version = "$mcVersion-1.0.0"

repositories {
    mavenCentral()
    maven("https://maven.kosmx.dev/")
    maven("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings("net.fabricmc:yarn:$mcVersion+build.3")
    modImplementation("net.fabricmc:fabric-loader:0.15.3")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.91.3+$mcVersion")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.10.16+kotlin.1.9.21")

    modImplementation("net.silkmc:silk-core:$silkVersion")
    modImplementation("net.silkmc:silk-commands:$silkVersion")
    modImplementation("net.silkmc:silk-network:$silkVersion")

    modImplementation("software.bernie.geckolib:geckolib-fabric-1.20.1:4.2.4")
    modImplementation("dev.kosmx.player-anim:player-animation-lib-fabric:1.0.2-rc1+1.20")
}

loom {
    accessWidenerPath.set(file("src/main/resources/ironman.accesswidener"))
}

tasks {
    compileKotlin {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjdk-release=$javaVersion", "-Xskip-prerelease-check")
            jvmTarget = "$javaVersion"
        }
    }
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(javaVersion)
    }
    processResources {
        val properties = mapOf("version" to project.version)
        inputs.properties(properties)
        filesMatching("fabric.mod.json") { expand(properties) }
    }
}
