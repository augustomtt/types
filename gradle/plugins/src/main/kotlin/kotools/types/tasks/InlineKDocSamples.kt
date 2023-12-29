/*
 * Copyright 2023 Kotools S.A.S.
 * Use of this source code is governed by the MIT license.
 */

package kotools.types.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File

/**
 * Task that inlines the `@sample` block tags in KDoc by their corresponding
 * function's body.
 */
@DisableCachingByDefault
public abstract class InlineKDocSamples : DefaultTask() {
    init {
        description =
            "Replaces samples in KDoc by their corresponding function's body."
        group(TaskGroup.DOCUMENTATION)
    }

    @TaskAction
    internal fun execute() {
        val tree: ConfigurableFileTree = project.fileTree("src") {
            include("*Main/**/*.kt")
            exclude("*Test/**/*.kt")
        }
        tree.asSequence()
            .mapNotNull { it }
            .forEach { original: File ->
                val lines: List<String> = original.readLines()
                original.writeText("") // Cleans the content of the file.

                val sampleTag = "@sample"
                val sampleRegex = Regex("$sampleTag [a-z]+(.[a-z]+)*")
                lines.forEach { line: String ->
                    if (line.contains(sampleRegex)) {
                        val prefix: String = line.replaceAfter(sampleTag, "")
                            .replace(sampleTag, "")
                        val identifier: String = line
                            .replaceBefore(sampleTag, "")
                            .replace("$sampleTag ", "")
                        val identifierParts: List<String> =
                            identifier.split('.')
                        val targetFilePath = original.path.split('/')
                            .joinToString("/") {
                                if (it == "kotlin") "kotlin/sample"
                                else if (it matches Regex("^[a-z]*Main$"))
                                    it.replace("Main", "Test")
                                else it
                            }
                        val functionTarget: String = identifierParts.last()
                        val target: File = project.file(targetFilePath)

                        val sampleBodyLines: MutableList<String> =
                            mutableListOf("```kotlin")
                        var readingSampleBody = false
                        target.forEachLine {
                            if (it.contains(functionTarget) && it.endsWith('{'))
                                readingSampleBody = true
                            else if (readingSampleBody && it.startsWith('}'))
                                readingSampleBody = false
                            else if (readingSampleBody)
                                sampleBodyLines += it.trimStart()
                        }
                        sampleBodyLines += "```"
                        val functionBody: String =
                            sampleBodyLines.joinToString("\n") { "$prefix$it" }
                        original.appendText("$functionBody\n")
                    } else original.appendText("$line\n")
                }
            }
    }
}

/** Task that restores the files updated by [InlineKDocSamples]. */
public abstract class InlineKDocSamplesCleanup : DefaultTask() {
    @TaskAction
    internal fun cleanup() {
        project.exec { commandLine("git", "restore", "src") }
    }
}
