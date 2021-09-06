package org.jetbrains.flcc.cli

import org.jetbrains.flcc.lang.Language
import java.lang.IllegalArgumentException
import java.nio.file.Path
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

data class CliOptions(
    val className: String?,
    val interfaceName: String?,
    val methodsWhitelist: List<String>,
    val methodsBlacklist: List<String>,
    val accessModifier: String?,
    val inputPath: Path,
    val outputPath: Path?,
    val inputLanguage: Language?,
    val outputLanguage: Language?
) {
    class Builder {
        private val className: StringFlag = StringFlag()
        private val interfaceName: StringFlag = StringFlag()
        private val methodsWhitelist: ListFlag = ListFlag()
        private val methodsBlacklist: ListFlag = ListFlag()
        private val accessModifier: StringFlag = StringFlag()
        private val inputPath: PathFlag = PathFlag()
        private val outputPath: PathFlag = PathFlag()
        private val inputLanguage: LanguageFlag = LanguageFlag()
        private val outputLanguage: LanguageFlag = LanguageFlag()

        fun set(name: String, value: String): Builder {
            for (prop in Builder::class.memberProperties) {
                if (prop.name.equals(name, ignoreCase = true)) {
                    prop.isAccessible = true
                    val flag = requireNotNull(prop.get(this) as? Flag<*>)
                    flag.setValue(value)
                    return this
                }
            }

            throw IllegalArgumentException("No property with name '$name'.")
        }

        fun build(): CliOptions {

            return CliOptions(
                className.value,
                interfaceName.value,
                methodsWhitelist.value,
                methodsBlacklist.value,
                accessModifier.value!!,
                inputPath.value ?: throw IllegalStateException("Property 'inputPath' must be initialized."),
                outputPath.value,
                inputLanguage.value,
                outputLanguage.value
            )
        }

        private sealed class Flag<T>(var value: T) {
            abstract fun setValue(stringValue: String)
        }

        private class StringFlag : Flag<String?>(null) {
            override fun setValue(stringValue: String) = run { value = stringValue }
        }

        private class ListFlag : Flag<List<String>>(emptyList()) {
            override fun setValue(stringValue: String) = run { value = stringValue.toListOfString() }
        }

        private class PathFlag : Flag<Path?>(null) {
            override fun setValue(stringValue: String) = run { value = Path.of(stringValue) }
        }

        private class LanguageFlag : Flag<Language?>(null) {
            override fun setValue(stringValue: String) {
                value = requireNotNull(Language.forName(stringValue)) {
                    "Cannot find a language by name '$stringValue'."
                }
            }
        }

        companion object {
            private fun String.toListOfString(): List<String> {
                check(first() == '[') { "Incorrect format for list." }
                check(last() == ']') { "Incorrect format for list." }
                return substring(1, length - 1).split(',').onEach { it.trim() }
            }
        }
    }
}
