package org.jetbrains.flcc.lang

import org.junit.jupiter.api.Test
import java.nio.file.Path

class KotlinLanguageTest : LanguageTest() {
    override val relativeTestPath get(): Path = TODO("implement Kotlin tests")

    @Test
    fun `test kotlin language existence`() = assertLanguageExistence(KotlinLanguage)
}
