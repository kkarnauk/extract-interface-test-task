package org.jetbrains.flcc.cli

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalArgumentException
import java.nio.file.Path

private typealias Option = Pair<String, String>

class ArgsParserTest {
    @Test
    fun `test input path`() = doTestOptions(emptyOptions().inputPath("test/Hello.java")) {
        assertEquals(Path.of("test", "Hello.java").toAbsolutePath(), input.path)
    }

    @Test
    fun `test output path`() {
        doTestOptions(defaultOptions()) {
            val (className, extension) = input.path.fileName.toString().split(".")
            assertEquals(input.path.parent.resolve("${className}Interface.$extension"), output.path)
        }
        doTestOptions(defaultOptions().outputPath("path/to/Test.java")) {
            assertEquals(Path.of("path", "to", "Test.java").toAbsolutePath(), output.path)
        }
    }

    @Test
    fun `test input language`() {
        doTestOptions(defaultOptions()) {
            assertEquals("java", input.language.name.lowercase())
            assertEquals("java", input.language.extension)
        }
        doTestOptions(defaultOptions().inputLanguage("Kotlin")) {
            assertEquals(input.language.name.lowercase(), "kotlin")
            assertEquals(input.language.extension, "kt")
        }
        assertThrows<IllegalArgumentException> {
            doTestOptions(defaultOptions().inputLanguage("NO_LANGUAGE_EVER")) { }
        }
    }

    @Test
    fun `test output language`() {
        doTestOptions(defaultOptions().outputLanguage("Java")) {
            assertEquals("java", output.language.name.lowercase())
            assertEquals("java", output.language.extension)
        }
    }

    @Test
    fun `test class name`() {
        doTestOptions(defaultOptions().className("MyClassName")) {
            assertEquals("MyClassName", input.name)
        }
        doTestOptions(defaultOptions(fileName = "HelloWorld.java")) {
            assertEquals("HelloWorld", input.name)
        }
    }

    @Test
    fun `test interface name`() {
        doTestOptions(defaultOptions().className("HeyEveryone")) {
            assertEquals("HeyEveryoneInterface", output.name)
        }
        doTestOptions(defaultOptions().interfaceName("MyNameIs")) {
            assertEquals("MyNameIs", output.name)
        }
    }

    @Test
    fun `test whitelist`() {
        doTestOptions(defaultOptions().whitelist("[one, SeCoNd]")) {
            assertEquals(listOf("one", "SeCoNd"), requirements.whitelist)
        }
        doTestOptions(defaultOptions()) {
            assertNull(requirements.whitelist)
        }
        assertThrows<IllegalArgumentException> {
            doTestOptions(defaultOptions().whitelist("[hello, world"))
        }
        assertThrows<IllegalArgumentException> {
            doTestOptions(defaultOptions().whitelist("hello, world"))
        }
    }

    @Test
    fun `test blacklist`() {
        doTestOptions(defaultOptions().blacklist("[kek, lol, another]")) {
            assertEquals(listOf("kek", "lol", "another"), requirements.blacklist)
        }
        doTestOptions(defaultOptions()) {
            assertNull(requirements.blacklist)
        }
    }

    @Test
    fun `test access modifier`() {
        doTestOptions(defaultOptions().accessModifier("private")) {
            assertEquals("private", requirements.accessModifier)
        }
        doTestOptions(defaultOptions()) {
            assertEquals("public", requirements.accessModifier)
        }
    }

    @Test
    fun `test multiple options`() {
        doTestOptions(
            defaultOptions("Main.kt")
                .whitelist("[one, two, three]")
                .blacklist("[first, second]")
                .inputLanguage("kotlin")
                .className("Main")
                .interfaceName("MainOtherInterface")
        ) {
            assertEquals(listOf("one", "two", "three"), requirements.whitelist)
            assertEquals(listOf("first", "second"), requirements.blacklist)
            assertEquals("kotlin", input.language.name.lowercase())
            assertEquals("Main", input.name)
            assertEquals("MainOtherInterface", output.name)
        }
    }

    @Test
    fun `test incorrect args`() {
        assertThrows<IllegalArgumentException> {
            doTestArgs(listOf("-inputPath", "-outputPath", "test/R.java", "test/RI.java"))
        }
        assertThrows<IllegalArgumentException> {
            doTestArgs(listOf("-inputPath", "Val.java", "-hello", "value"))
        }
        assertThrows<IllegalArgumentException> { // no inputPath provided
            doTestArgs(listOf("-whitelist [fir, sec]"))
        }
    }

    private inline fun doTestOptions(options: List<Option>, check: Options.() -> Unit = { }) {
        mutableListOf<String>().apply {
            options.forEach { (name, value) -> add("-$name"); add(value) }
            doTestArgs(this, check)
        }
    }

    private inline fun doTestArgs(args: List<String>, check: Options.() -> Unit = { }) {
        ArgsParser.parse(args).check()
    }

    private fun emptyOptions() = mutableListOf<Option>()

    private fun defaultOptions(fileName: String = "TestClass.java") = emptyOptions().apply {
        inputPath("/path/to/directory/$fileName")
    }

    private fun MutableList<Option>.inputPath(value: String) = apply { add("inputPath" to value) }
    private fun MutableList<Option>.outputPath(value: String) = apply { add("outputPath" to value) }
    private fun MutableList<Option>.className(value: String) = apply { add("className" to value) }
    private fun MutableList<Option>.interfaceName(value: String) = apply { add("interfaceName" to value) }
    private fun MutableList<Option>.inputLanguage(value: String) = apply { add("inputLanguage" to value) }
    private fun MutableList<Option>.outputLanguage(value: String) = apply { add("outputLanguage" to value) }
    private fun MutableList<Option>.whitelist(value: String) = apply { add("whitelist" to value) }
    private fun MutableList<Option>.blacklist(value: String) = apply { add("blacklist" to value) }
    private fun MutableList<Option>.accessModifier(value: String) = apply { add("accessModifier" to value) }
}
