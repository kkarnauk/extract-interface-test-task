package org.jetbrains.flcc.lang

import org.jetbrains.flcc.cli.Options
import org.junit.jupiter.api.Test
import java.nio.file.Path

class KotlinToJavaLanguageTest : LanguageTest() {
    override val relativeTestPath: Path = Path.of("kotlinToJava")

    override fun defaultOptions(inputFilename: String, outputFilename: String): Options.Builder =
        super.defaultOptions(inputFilename, outputFilename).apply {
            inputLanguage.value = KotlinLanguage
        }

    @Test
    fun `test simple class`() = doTest("SimpleClass.kt", "ISimpleClass.java") {
        blacklist.value = listOf("noway")
    }

    @Test
    fun `test generics`() = doTest("Generics.kt", "Generics.java")

    @Test
    fun `test types`() = doTest("Types.kt", "TypesInterface.java")
}
