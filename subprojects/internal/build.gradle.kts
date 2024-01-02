@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("kotools.types.base")
    id("kotools.types.multiplatform")
}

repositories.mavenCentral()

dependencies {
    commonMainImplementation(libs.kotlinx.serialization.core)
    commonTestImplementation(libs.kotlin.test)
}
