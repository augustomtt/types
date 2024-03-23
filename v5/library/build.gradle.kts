import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("kotools.types.multiplatform")
    alias(libs.plugins.dokka)
    id("kotools.types.documentation")
    alias(libs.plugins.kotlinx.binary.compatibility.validator)
}

apiValidation.apiDumpDirectory = "src/api"
documentation {
    license = rootProject.layout.projectDirectory.file("../LICENSE.txt").asFile
    logo = rootProject.layout.projectDirectory.file("../dokka/logo-icon.svg")
        .asFile
    moduleName = "Kotools Types 5"
}

dependencies {
    commonMainImplementation(platform(libs.kotlin.bom))
    commonTestImplementation(libs.kotlin.test)
}

tasks.jsBrowserTest.configure(KotlinJsTest::useMocha)
tasks.withType<DokkaTask>().configureEach {
    failOnWarning.set(true)
    dokkaSourceSets.configureEach {
        val moduleDocumentation: RegularFile =
            layout.projectDirectory.file("module.md")
        includes.setFrom(moduleDocumentation)
        reportUndocumented.set(true)
    }
}
