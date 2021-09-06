package org.jetbrains.flcc.lang

import com.github.javaparser.ast.body.MethodDeclaration
import java.io.File

sealed class Language {
    abstract val name: String
    abstract val extension: String

    abstract fun extractMethods(
        code: String,
        className: String,
        methodFilter: (MethodDeclaration) -> Boolean
    ): List<MethodSignature>

    abstract fun constructInterface(
        interfaceName: String,
        methods: List<MethodSignature>
    ): String

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