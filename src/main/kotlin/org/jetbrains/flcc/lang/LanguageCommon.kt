package org.jetbrains.flcc.lang

/**
 * **L**anguage **C**ommon description for classes and interfaces
 */
data class ClassOrInterfaceLC(
    val name: String,
    val methods: List<MethodLC>,
    val typeParameters: List<TypeParameterLC>
)

/**
 * **L**anguage **C**ommon description for methods.
 */
data class MethodLC(
    val name: String,
    val returnType: TypeLC,
    val parameters: List<ParameterLC>,
    val typeParameters: List<TypeParameterLC>,
    val accessModifier: AccessModifierLC
)

/**
 * **L**anguage **C**ommon description for parameters of methods.
 */
data class ParameterLC(val name: String, val type: TypeLC)

/**
 * **L**anguage **C**ommon description for access modifiers.
 */
// TODO make possible to choose for different languages different acceptable modifiers.
@JvmInline
value class AccessModifierLC(val name: String)

/**
 * **L**anguage **C**ommon description for types.
 */
@JvmInline
value class TypeLC(val name: String)

/**
 * **L**anguage **C**ommon description for type parameters of classes and methods.
 */
data class TypeParameterLC(
    val name: String,
    val bound: List<TypeLC>
)
