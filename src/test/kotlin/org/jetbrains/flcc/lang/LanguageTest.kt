package org.jetbrains.flcc.lang

import org.jetbrains.flcc.cli.Options
import org.jetbrains.flcc.extractInfoAndConstructInterface
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.readText

abstract class LanguageTest {
    protected abstract val relativeTestPath: Path

    private val baseTestPath: Path by lazy {
        Path.of("src", "test", "resources", "testData").resolve(relativeTestPath)
    }

    private val inputDir: Path by lazy { baseTestPath.resolve("input") }

    private val outputPath: Path by lazy { baseTestPath.resolve("output") }

    private val tempDir: Path = createTempDirectory()

    protected fun assertLanguageExistence(language: Language) {
        assertSame(language, Language.forName(language.name))
    }

    protected open fun defaultOptions(inputFilename: String, outputFilename: String) = Options.Builder().apply {
        inputPath.value = inputDir.resolve(inputFilename)
        outputPath.value = tempDir.resolve(outputFilename)
        className.value = inputFilename.substringBeforeLast(".")
        interfaceName.value = outputFilename.substringBeforeLast(".")
    }

    protected fun doTest(
        inputFilename: String,
        outputFilename: String,
        apply: Options.Builder.() -> Unit = { }
    ) = defaultOptions(inputFilename, outputFilename).run {
        apply()
        doTest(this)
    }

    private fun doTest(optionsBuilder: Options.Builder) {
        val options = optionsBuilder.build()
        val filename = options.output.language.nameForFile(options.output.name)
        val expectedCode = outputPath.resolve(filename).readText()
        doTest(options, expectedCode)
    }

    private fun doTest(options: Options, expectedCode: String) {
        extractInfoAndConstructInterface(options)
        val actualCode = options.output.path.readText()
        assertEquals(expectedCode, actualCode)
    }
}
