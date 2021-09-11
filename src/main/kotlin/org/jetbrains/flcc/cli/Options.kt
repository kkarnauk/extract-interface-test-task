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
        val className: StringFlag = StringFlag()
        val interfaceName: StringFlag = StringFlag()
        val whitelist: ListFlag = ListFlag()
        val blacklist: ListFlag = ListFlag()
        val accessModifier: StringFlag = StringFlag()
        val inputPath: PathFlag = PathFlag()
        val outputPath: PathFlag = PathFlag()
        val inputLanguage: LanguageFlag = LanguageFlag()
        val outputLanguage: LanguageFlag = LanguageFlag()

        fun set(name: String, value: String): Builder {
            for (prop in Builder::class.memberProperties) {
                if (prop.name.equals(name, ignoreCase = true)) {
                    prop.isAccessible = true
                    val flag = requireNotNull(prop.get(this) as? CommandFlag<*>)
                    flag.setValue(value)
                    return this
                }
            }

            throw IllegalArgumentException("No property with name '$name'.")
        }

        fun build(): Options {
            val inputLanguage = inputLanguage.value ?: defaultLanguage()
            val outputLanguage = outputLanguage.value ?: defaultLanguage()
            val inputPath = requireNotNull(inputPath.value) { ("Property 'inputPath' must be initialized.") }
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

        companion object {
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
