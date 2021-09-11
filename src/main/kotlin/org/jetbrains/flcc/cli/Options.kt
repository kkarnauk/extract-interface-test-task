package org.jetbrains.flcc.cli

import org.jetbrains.flcc.lang.JavaLanguage
import org.jetbrains.flcc.lang.Language
import java.nio.file.Path
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

data class Options(
    val input: IOOptions,
    val output: IOOptions,
    val requirements: MethodsRequirements
) {
    data class IOOptions(
        val name: String,
        val path: Path,
        val language: Language
    )

    data class MethodsRequirements(
        val whitelist: List<String>?,
        val blacklist: List<String>?,
        val accessModifier: String
    )

    class Builder {
        private val className: StringFlag = StringFlag()
        private val interfaceName: StringFlag = StringFlag()
        private val whitelist: ListFlag = ListFlag()
        private val blacklist: ListFlag = ListFlag()
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

        fun build(): Options {
            val inputLanguage = inputLanguage.value ?: defaultLanguage()
            val outputLanguage = outputLanguage.value ?: defaultLanguage()
            val inputPath = inputPath.value ?: throw IllegalStateException("Property 'inputPath' must be initialized.")
            val className = className.value ?: defaultClassName(inputPath, inputLanguage)
            val interfaceName = interfaceName.value ?: defaultInterfaceName(className)
            val outputPath = outputPath.value ?: defaultOutputPath(inputPath, interfaceName, outputLanguage)
            val accessModifier = accessModifier.value ?: defaultAccessModifier()
            return Options(
                IOOptions(
                    className,
                    inputPath,
                    inputLanguage
                ),
                IOOptions(
                    interfaceName,
                    outputPath,
                    outputLanguage
                ),
                MethodsRequirements(
                    whitelist.value,
                    blacklist.value,
                    accessModifier
                )
            )
        }

        private sealed class Flag<T>(var value: T) {
            abstract fun setValue(stringValue: String)
        }

        private class StringFlag : Flag<String?>(null) {
            override fun setValue(stringValue: String) = run { value = stringValue }
        }

        private class ListFlag : Flag<List<String>?>(null) {
            override fun setValue(stringValue: String) = run { value = stringValue.toListOfString() }
        }

        private class PathFlag : Flag<Path?>(null) {
            override fun setValue(stringValue: String) = run { value = Path.of(stringValue).toAbsolutePath() }
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
                require(first() == '[') { "Incorrect format for list." }
                require(last() == ']') { "Incorrect format for list." }
                return substring(1, length - 1).split(',').map { it.trim() }
            }

            private fun defaultLanguage(): Language = JavaLanguage

            private fun defaultClassName(inputPath: Path, inputLanguage: Language): String =
                inputLanguage.primaryClassNameForFile(inputPath.toFile())

            private fun defaultInterfaceName(className: String): String = className + "Interface"

            private fun defaultAccessModifier(): String = "public"

            private fun defaultOutputPath(inputPath: Path, interfaceName: String, outputLanguage: Language): Path {
                val fileName = outputLanguage.nameForFile(interfaceName)
                return requireNotNull(inputPath.parent) { "'inputPath' must be a file" }.resolve(fileName)
            }
        }
    }
}
