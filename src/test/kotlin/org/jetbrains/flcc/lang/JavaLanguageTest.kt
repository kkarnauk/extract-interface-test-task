package org.jetbrains.flcc.lang

import org.junit.jupiter.api.Test
import java.nio.file.Path

class JavaLanguageTest : LanguageTest() {
    override val relativeTestPath: Path = Path.of("java")

    @Test
    fun `test java language existence`() = assertLanguageExistence(JavaLanguage)

    @Test
    fun `test simple class`() = doTest("SimpleClass.java", "SimpleClassInterface.java")

    @Test
    fun `test whitelist`() = doTest("Whitelist.java", "WhitelistInterface.java") {
        whitelist.value = listOf("hello", "KeK")
        outputLanguage.value = JavaLanguage
    }

    @Test
    fun `test blacklist`() = doTest("Blacklist.java", "BlacklistInterface.java") {
        blacklist.value = listOf("noway", "noway2")
        inputLanguage.value = JavaLanguage
    }

    @Test
    fun `test access modifier`() = doTest("AccessModifier.java", "IAccessModifier.java") {
        accessModifier.value = "private"
    }

    @Test
    fun `test class generics`() = doTest("ClassGenerics.java", "ClassGenerics.java") {
        accessModifier.value = "protected"
    }

    @Test
    fun `test class multiple generics`() = doTest("MultipleClassGenerics.java", "MultipleClassGenerics.java")

    @Test
    fun `test methods generics`() = doTest("MethodsGenerics.java", "MethodsGenericsI.java") {
        accessModifier.value = "private"
        blacklist.value = listOf("two")
    }

    @Test
    fun `test multiple top level classes`() = doTest("MultipleTopLevelClasses.java", "MultipleTopLevelClasses.java")

    @Test
    fun `test nested class`() = doTest("OuterClass.java", "NestedClass.java") {
        className.value = "NestedClass"
    }

    @Test
    fun `test deep nested class`() = doTest("DeepNestedClass.java", "Nested5.class") {
        className.value = "Nested5"
    }
}
