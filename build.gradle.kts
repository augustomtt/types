import org.jetbrains.dokka.versioning.VersioningConfiguration
import org.jetbrains.dokka.versioning.VersioningPlugin
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl

plugins {
    val kotlinVersion = "1.7.21"
    kotlin("multiplatform") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("org.jetbrains.dokka") version "1.7.20"
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.12.1"
    `maven-publish`
    signing
}

buildscript {
    dependencies { classpath("org.jetbrains.dokka:versioning-plugin:1.7.20") }
}

group = "org.kotools"
version = "4.3.0"
val projectName = "Kotools Types"

repositories(RepositoryHandler::mavenCentral)

dependencies {
    // Kotlin
    commonMainImplementation(platform(kotlin("bom")))
    commonTestImplementation(kotlin("test"))

    // Kotlinx Serialization
    fun kotlinxSerialization(module: String): String {
        val group = "org.jetbrains.kotlinx"
        val artifact = "kotlinx-serialization-$module"
        val version = "1.4.1"
        return "$group:$artifact:$version"
    }
    commonMainImplementation(kotlinxSerialization("core"))
    commonTestImplementation(kotlinxSerialization("json"))

    // Dokka
    dokkaHtmlPlugin("org.jetbrains.dokka:versioning-plugin:1.7.20")
}

kotlin {
    explicitApi()
    js(IR, KotlinJsTargetDsl::browser)
    jvm {
        compilations.all { kotlinOptions.jvmTarget = "17" }
        testRuns["test"].executionTask { useJUnitPlatform() }
    }
    linuxX64("linux")
    macosX64("macos")
    mingwX64("windows")
}

tasks.withType<Jar> {
    fun key(suffix: String): String = "Implementation-$suffix"
    val name: Pair<String, String> = key("Title") to project.name
    val version: Pair<String, Any> = key("Version") to project.version
    manifest.attributes(name, version)
}
val dokkaDirectory: File = buildDir.resolve("dokka")
tasks.dokkaHtml {
    outputDirectory.set(dokkaDirectory)
    dokkaSourceSets {
        configureEach {
            includes.from += "packages.md"
            reportUndocumented.set(true)
            skipEmptyPackages.set(true)
        }
    }
    moduleName.set(projectName)
    val currentVersion = "${project.version}"
    val apiReferencesDir: File = rootDir.resolve("api/references")
    pluginConfiguration<VersioningPlugin, VersioningConfiguration> {
        version = currentVersion
        olderVersionsDir = apiReferencesDir
    }
    doLast {
        val images = "images"
        val logo = "logo-icon.svg"
        val dokkaImages: File = dokkaDirectory.resolve(images)
        val kotlinLogo: File = dokkaImages.resolve(logo)
        delete(kotlinLogo)
        copy {
            val kotoolsLogo: File = rootDir.resolve(images)
                .resolve(logo)
            from(kotoolsLogo)
            into(dokkaImages)
        }
        if ("SNAPSHOT" !in currentVersion) {
            copy {
                from(dokkaDirectory)
                into(apiReferencesDir.resolve(currentVersion))
            }
            copy {
                from(dokkaDirectory)
                into(rootDir.resolve("docs"))
            }
            delete(apiReferencesDir.resolve(currentVersion).resolve("older"))
        }
    }
}
val cleanDokkaHtml: TaskProvider<Delete> =
    tasks.register<Delete>("cleanDokkaHtml") { delete(dokkaDirectory) }
val javadocJar: TaskProvider<Jar> = tasks.register<Jar>("javadocJar") {
    dependsOn(cleanDokkaHtml, tasks.dokkaHtml)
    archiveClassifier.set("javadoc")
    from(tasks.dokkaHtml)
}
tasks.assemble { dependsOn(javadocJar) }

publishing {
    repositories {
        maven {
            name = "OSSRH"
            url = uri(
                "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            )
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
    }
    publications {
        getByName("kotlinMultiplatform", MavenPublication::class) {
            groupId = "${project.group}"
            artifactId = project.name
            version = "${project.version}"
        }
        forEach {
            if (it !is MavenPublication) return@forEach
            it.artifact(javadocJar)
            it.pom {
                name.set(projectName)
                description.set(
                    "Multiplatform library providing explicit types for Kotlin."
                )
                val gitRepository = "https://github.com/kotools/types"
                url.set(gitRepository)
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                issueManagement {
                    system.set("GitHub")
                    url.set("$gitRepository/issues")
                }
                scm {
                    connection.set("$gitRepository.git")
                    url.set(gitRepository)
                }
                developers {
                    developer {
                        name.set(System.getenv("GIT_USER"))
                        email.set(System.getenv("GIT_EMAIL"))
                    }
                }
            }
            signing {
                val secretKey: String? = System.getenv("GPG_PRIVATE_KEY")
                val password: String? = System.getenv("GPG_PASSWORD")
                useInMemoryPgpKeys(secretKey, password)
                sign(it)
            }
        }
    }
}
