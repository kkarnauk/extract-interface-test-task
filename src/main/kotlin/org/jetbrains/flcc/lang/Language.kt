package org.jetbrains.flcc.lang

import java.io.File

sealed class Language {
    abstract val name: String
    abstract val extension: String

    open fun extractMethods(
        code: String,
        className: String,
        methodFilter: MethodFilter
    ): List<MethodSignature> = throw UnsupportedOperationException("Language '$name' cannot extract methods.")

    open fun constructInterface(
        interfaceName: String,
        methods: List<MethodSignature>
    ): String = throw UnsupportedOperationException("Language '$name' cannot construct interfaces.")

    open fun primaryClassNameForFile(file: File): String = file.nameWithoutExtension

    open fun nameForFile(className: String): String = "$className.$extension"

    companion object {
        fun forName(name: String): Language? {
            return Language::class.sealedSubclasses
                .mapNotNull { it.objectInstance }
                .firstOrNull { it.name == name }
        }
    }
}
