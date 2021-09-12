package org.jetbrains.flcc.lang

import java.io.File

/**
 * Represents a programming language.
 * Required to extract description from classes and construct new interfaces from descriptions.
 */
sealed class Language {
    /**
     * Name of language
     */
    abstract val name: String

    /**
     * Primary extension of language
     */
    abstract val extension: String

    /**
     * @return full Language Common description from class with name [className] in [code].
     * @throws UnsupportedOperationException if this language doesn't provide such functionality.
     */
    open fun extractClassDescription(
        code: String,
        className: String
    ): ClassOrInterfaceLC = throw UnsupportedOperationException("Language '$name' cannot extract methods.")

    /**
     * @return interface code produced using [interfaceDescription].
     */
    open fun constructInterfaceCode(
        interfaceDescription: ClassOrInterfaceLC
    ): String = throw UnsupportedOperationException("Language '$name' cannot construct interfaces.")

    /**
     * Required to make types from different languages compatible.
     *
     * For example, Java doesn't support Kotlin's `?` in types.
     *
     * The reason why it's `'to'` and not `'from'` is
     * because you'll probably need some private utilities methods for current language.
     */
    open fun convertTypeToOtherLanguage(type: TypeLC, otherLanguage: Language): TypeLC = type

    /**
     * Required if a user didn't provide `className` in a configuration.
     * @return primary class name from [file] of this language.
     */
    open fun primaryClassNameForFile(file: File): String = file.nameWithoutExtension

    /**
     * Required if a user didn't provide `interfaceName` in a configuration.
     * @return filename for a [className].
     */
    open fun nameForFile(className: String): String = "$className.$extension"

    companion object {
        /**
         * Searches for supported [Language] in which [Language.name] equals to [name].
         * Returns `null` if no language with [name] is supported.
         */
        fun forName(name: String): Language? {
            return Language::class.sealedSubclasses
                .mapNotNull { it.objectInstance }
                .firstOrNull { it.name.equals(name, ignoreCase = true) }
        }
    }
}
