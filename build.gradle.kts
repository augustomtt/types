import kotools.types.tasks.InlineKDocSamples
import kotools.types.tasks.InlineKDocSamplesCleanup

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinx.binary.compatibility.validator)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.dokka)
    `maven-publish`
    signing
    id("kotools.types.base")
    id("kotools.types.multiplatform")
    id("kotools.types.documentation")
    id("kotools.types.publication")
}

group = "org.kotools"

repositories.mavenCentral()

publishing.publications.named<MavenPublication>("kotlinMultiplatform")
    .configure {
        groupId = "${project.group}"
        artifactId = project.name
        version = "${project.version}"
    }

dependencies {
    commonMainImplementation(platform(libs.kotlin.bom))
    commonMainImplementation(projects.internal)
    commonMainImplementation(libs.kotlinx.serialization.core)

    commonTestImplementation(libs.kotlin.test)
    commonTestImplementation(libs.kotlinx.serialization.json)

    dokkaHtmlPlugin(libs.dokka.versioning)
}

tasks {
    register("unit")

    val inlineKDocSamples: TaskProvider<InlineKDocSamples> =
        register<InlineKDocSamples>("inlineKDocSamples")
    val dokkaSamplesCleanup: TaskProvider<InlineKDocSamplesCleanup> =
        register<InlineKDocSamplesCleanup>("inlineKDocSamplesCleanup")
    dokkaHtml.configure {
        dependsOn += inlineKDocSamples
        finalizedBy(dokkaSamplesCleanup)
    }
}
