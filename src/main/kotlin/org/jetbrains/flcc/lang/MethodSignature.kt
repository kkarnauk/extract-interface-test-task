package org.jetbrains.flcc.lang

data class MethodSignature(
    val name: String,
    val returnType: Type,
    val parameters: List<Parameter>,
) {
    @JvmInline
    value class Type(val name: String)

    data class Parameter(val name: String, val type: Type)
}
