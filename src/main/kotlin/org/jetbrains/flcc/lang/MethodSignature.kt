package org.jetbrains.flcc.lang

data class MethodSignature(
    val name: String,
    val returnType: Type,
    val parameters: List<Parameter>,
    val accessModifier: AccessModifier
) {
    @JvmInline
    value class Type(val name: String)

    @JvmInline
    value class AccessModifier(val name: String)

    data class Parameter(val name: String, val type: Type)
}

typealias MethodFilter = (MethodSignature) -> Boolean
