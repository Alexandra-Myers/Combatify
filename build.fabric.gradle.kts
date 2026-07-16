@file:Suppress("UnstableApiUsage")

import java.util.Locale


plugins {
    id("dev.kikugie.loom-back-compat")
    id("dev.kikugie.postprocess.jsonlang")
    id("me.modmuss50.mod-publish-plugin")
}

tasks.named<ProcessResources>("processResources") {
    fun prop(name: String) = project.property(name) as String

    val props = HashMap<String, String>().apply {
        this["version"] = prop("mod.version") + "-" + prop("deps.minecraft")
        this["minecraft"] = prop("mod.mc_dep_fabric")
        this["fabric_api_version"] = prop("deps.fabric-api")
        this["fabric_version"] = prop("deps.fabric-loader")
        this["java"] = prop("deps.java")
        this["mod_name"] = prop("mod.name")
        this["mod_description"] = prop("mod.description")
        this["mod_license"] = prop("mod.license")
        this["mod_id"] = prop("mod.id")
        this["defaulted_version"] = prop("deps.dep_defaulted")
        this["atlas_core_version"] = prop("deps.atlas_core")
    }

    filesMatching(listOf("fabric.mod.json", "META-INF/neoforge.mods.toml")) {
        expand(props)
    }
}

version = "${property("mod.version")}.${property("mod.sub_version")}-${property("deps.minecraft")}-fabric"
base.archivesName = property("mod.archives_base") as String

loom {
    accessWidenerPath = project.file("src/main/resources/${property("mod.id")}.classtweaker")
}

jsonlang {
    languageDirectories = listOf("assets/${property("mod.id")}/lang")
    prettyPrint = true
}

repositories {
    // Local stuff
    flatDir {
        dirs = setOf(File("libs"))
    }

    maven {
        name = "shedaniel (Cloth Config)"
        url = uri("https://maven.shedaniel.me/")
        content {
            includeGroupAndSubgroups("me.shedaniel")
        }
    }
    maven {
        name = "Terraformers (Mod Menu)"
        url = uri("https://maven.terraformersmc.com/releases/")
        content {
            includeGroupAndSubgroups("com.terraformersmc")
            includeGroupAndSubgroups("dev.emi")
        }
    }
    maven {
        name = "Wisp Forest Maven"
        url = uri("https://maven.wispforest.io/releases/")
        content {
            includeGroupAndSubgroups("io.wispforest")
        }
    }
    maven {
        name = "Modrinth"
        url = uri("https://api.modrinth.com/maven")
        content {
            includeGroupAndSubgroups("maven.modrinth")
        }
    }
    maven {
        name = "WTHIT"
        url = uri("https://maven2.bai.lol")
        content {
            includeGroupAndSubgroups("mcp.mobius.waila")
            includeGroupAndSubgroups("lol.bai")
        }
    }
    maven {
        name = "Sisby Maven"
        url = uri("https://repo.sleeping.town/")
        content {
            includeGroupAndSubgroups("folk.sisby")
        }
    }
    maven {
        name = "Parchment Mappings"
        url = uri("https://maven.parchmentmc.org")
        content {
            includeGroupAndSubgroups("org.parchmentmc")
        }
    }
    maven {
        name = "Xander Maven"
        url = uri("https://maven.isxander.dev/releases")
        content {
            includeGroupAndSubgroups("dev.isxander")
            includeGroupAndSubgroups("org.quiltmc.parsers")
        }
    }
    maven {
        name = "Nucleoid Maven (Polymer/Trinkets)"
        url = uri("https://maven.nucleoid.xyz")
        content {
            includeGroupAndSubgroups("eu.pb4")
            includeGroupAndSubgroups("xyz.nucleoid")
        }
    }
    maven {
        name = "Fuzs Mod Resources"
        url = uri("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/")
        content {
            includeGroupAndSubgroups("fuzs")
        }
    }
    maven {
        name = "Architectury"
        url = uri("https://maven.architectury.dev/")
        content {
            includeGroup("dev.architectury")
        }
    }
    maven {
        name = "Jitpack"
        url = uri("https://jitpack.io")
        content {
            includeGroup("com.github.Chocohead")
            includeGroup("com.github.oryxel1")
            includeGroup("com.github.Oryxel") // oh my fucking god dude
        }
    }
    exclusiveContent {
        forRepository {
            maven {
              name = "Cassian's Maven"
              url = uri("https://maven.cassian.cc")
            }
        }
        filter {
            includeGroupAndSubgroups("cc.cassian")
        }
    }
    mavenCentral()

    // Hello this is where we use the mavens lets go
    maven {
        name = "ViaVersion"
        url = uri("https://repo.viaversion.com/everything/")
        content {
            includeGroup("com.viaversion")
            includeGroup("com.viaversion.mcstructs")
            includeGroup("net.raphimc")
            includeGroup("de.florianmichael")
        }
    }
    maven {
        name = "Lenni0451"
        url = uri("https://maven.lenni0451.net/everything")
    }
    maven {
        name = "OpenCollab Snapshots"
        url = uri("https://repo.opencollab.dev/maven-snapshots/")
    }
    maven {
        url = uri("https://maven.ryanliptak.com/")
    }
    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = uri("https://api.modrinth.com/maven")
            }
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${property("deps.minecraft")}")
    loomx.applyMojangMappings()
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric-loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric-api")}")
    modImplementation("com.terraformersmc:modmenu:${property("deps.modmenu")}")
    modApi("me.shedaniel.cloth:cloth-config-fabric:${property("deps.cloth-config")}")

    modImplementation("maven.modrinth:sodium:${property("deps.sodium")}")
    modImplementation("maven.modrinth:atlas-core:${property("deps.atlas_core")}-Fabric")
    modImplementation("maven.modrinth:defaulted:${property("deps.defaulted")}")
    if (stonecutter.eval(stonecutter.current.version, ">=1.21.4")) modImplementation("com.viaversion:viafabricplus:${property("deps.viafabricplus")}")
    else modImplementation("de.florianmichael:ViaFabricPlus:${property("deps.viafabricplus")}")
    modApi("com.terraformersmc:modmenu:${property("deps.modmenu")}")
    modImplementation("maven.modrinth:cookeymod:${property("deps.cookeymod")}")
    modImplementation("eu.pb4:polymer-core:${property("deps.polymer")}")
    modImplementation("squeek.appleskin:appleskin-fabric:${property("deps.appleskin")}")
    implementation("io.github.java-diff-utils:java-diff-utils:${property("deps.java_diff_version")}")
    implementation("org.mozilla:rhino-all:${property("deps.rhino")}")

    implementation("com.fasterxml.jackson.core:jackson-core:${property("deps.jackson")}")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-toml:${property("deps.jackson")}")
}


configurations.all {
    resolutionStrategy {
        force("net.fabricmc:fabric-loader:${property("deps.fabric-loader")}")
        force("net.fabricmc:fabric-api:${property("deps.fabric-api")}")
    }
}


fabricApi {
    configureDataGeneration() {
        outputDirectory = file("$rootDir/src/main/generated")
        client = true
    }
}

tasks.named("processResources") {
    dependsOn(":${stonecutter.current.project}:stonecutterGenerate")
}

tasks {
    processResources {
        exclude("**/neoforge.mods.toml")
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        from(loomx.modJar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
        dependsOn("build")
    }
}

java {
    withSourcesJar()
    val javaCompat = if (stonecutter.eval(stonecutter.current.version, ">=26")) {
        JavaVersion.VERSION_25
    } else if (stonecutter.eval(stonecutter.current.version, ">=1.21")) {
        JavaVersion.VERSION_21
    } else {
        JavaVersion.VERSION_17
    }
    sourceCompatibility = javaCompat
    targetCompatibility = javaCompat
}

stonecutter {
    val (version, loader) = current.project.split('-', limit = 2)
    properties.tags(version, loader)

    replacements.string(current.parsed >= "26.1.2") {
        replace("FabricDataOutput", "FabricPackOutput")
    }

    replacements.string(current.parsed >= "1.21.11") {
        replace("ResourceLocation", "Identifier")
        replace("location()", "identifier()")
        replace("net.minecraft.Util", "net.minecraft.util.Util")
        replace("net.minecraft.FileUtil", "net.minecraft.util.FileUtil")
        replace("org.jetbrains.annotations.Nullable", "org.jspecify.annotations.Nullable")
        replace("org.jetbrains.annotations.NotNull", "org.jspecify.annotations.NonNull")
        replace("@NotNull", "@NonNull")
    }

    replacements.string(current.parsed >= "1.21.5") {
        replace("net.atlas.combatify.util.blocking.DamageReduction", "net.minecraft.world.item.component.BlocksAttacks.DamageReduction")
        replace("net.atlas.combatify.util.blocking.ItemDamageFunction", "net.minecraft.world.item.component.BlocksAttacks.ItemDamageFunction")
    }

    replacements.string(current.parsed >= "1.21.4") {
        replace("net.minecraft.world.item.Tier", "net.minecraft.world.item.ToolMaterial")
        replace("Tiers.", "ToolMaterial.")
    }
}

val additionalVersionsStr = findProperty("publish.additionalVersions") as String?
val additionalVersions: List<String> = additionalVersionsStr
    ?.split(",")
    ?.map { it.trim() }
    ?.filter { it.isNotEmpty() }
    ?: emptyList()

publishMods {
    file = tasks.jar.map { it.archiveFile.get() }
    additionalFiles.from(tasks.named<org.gradle.jvm.tasks.Jar>("sourcesJar").map { it.archiveFile.get() })

    var release = "${property("mod.sub_version")}" == "release"
    type =
        if (release) STABLE
        else BETA
    var subVer =
        if (release) ""
        else ".${property("mod.sub_version")}"
    var displaySubVer =
        if (release) ""
        else " ${(property("mod.sub_version") as String).replace(".", " ").uppercase(Locale.getDefault())}"

    displayName = "${property("mod.name")} ${property("mod.version")} $displaySubVer ${stonecutter.current.version} Fabric"
    version = "${property("mod.version")}${subVer}-${property("deps.minecraft")}-Fabric"
    changelog = provider { rootProject.file("changelog.md").readText() }
    modLoaders.add("neoforge")

    modrinth {
        projectId = property("publish.modrinth") as String
        accessToken = env.MODRINTH_API_KEY.orNull()
        minecraftVersions.add(property("deps.minecraft") as String)
        minecraftVersions.addAll(additionalVersions)
        requires("atlas-core", "defaulted", "cloth-config", "fabric-api")
        optional("polymer", "modmenu", "cookeymod")
        environment = SERVER_ONLY_CLIENT_OPTIONAL
    }

    curseforge {
        projectId = property("publish.curseforge") as String
        accessToken = env.CURSEFORGE_API_KEY.orNull()
        minecraftVersions.add(property("deps.minecraft") as String)
        minecraftVersions.addAll(additionalVersions)
        requires("atlas-core", "defaulted", "cloth-config", "fabric-api")
        optional("polymer", "modmenu")
        javaVersions.add(if (stonecutter.eval(stonecutter.current.version, ">=26")) {
            JavaVersion.VERSION_25
        } else if (stonecutter.eval(stonecutter.current.version, ">=1.20.5")) {
            JavaVersion.VERSION_21
        } else {
            JavaVersion.VERSION_17
        })
        changelogType = "markdown"
        client = true
        server = true
    }
}
