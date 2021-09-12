package org.jetbrains.flcc.cli

import org.jetbrains.flcc.lang.JavaLanguage
import org.jetbrains.flcc.lang.Language
import java.nio.file.Path
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

/**
 * Represents parsed command line options.
 * If no value is provided for an option, then a default one will be used.
 */
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

    /**
     * Serves to iteratively build [Options].
     */
    class Builder {
        /**
         * Input class name.
         * @see IOOptions.name
         */
        val className: StringFlag = StringFlag()

        /**
         * Output interface name.
         * @see IOOptions.name
         */
        val interfaceName: StringFlag = StringFlag()

        /**
         * List of allowed methods names. If no list is provided, then all methods are in this list.
         * @see MethodsRequirements.whitelist
         */
        val whitelist: ListFlag = ListFlag()

        /**
         * List of forbidden methods names. If no list is provided, then no methods are in this list.
         * @see MethodsRequirements.blacklist
         */
        val blacklist: ListFlag = ListFlag()

        /**
         * Required access modifier of methods to be extracted. It's `'public'` by default
         * @see MethodsRequirements.accessModifier
         */
        val accessModifier: StringFlag = StringFlag()

        /**
         * Input file path. You **must** provide it.
         * @see IOOptions.path
         */
        val inputPath: PathFlag = PathFlag()

        /**
         * Output file path. By default it's `parent of [inputPath] + [interfaceName] + .extension`.
         * @see IOOptions.path
         */
        val outputPath: PathFlag = PathFlag()

        /**
         * Language for input class. It's [JavaLanguage] by default.
         * @see IOOptions.language
         */
        val inputLanguage: LanguageFlag = LanguageFlag()

        /**
         * Language for output interface. It's [JavaLanguage] by default.
         * @see IOOptions.language
         */
        val outputLanguage: LanguageFlag = LanguageFlag()

        /**
         * Searches [name] in flag properties and sets [value] using [CommandFlag.setValue].
         */
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

        /**
         * Converts [Builder] in [Options].
         */
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
