package org.jetbrains.flcc.lang

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.AccessSpecifier
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.body.Parameter
import com.github.javaparser.ast.body.TypeDeclaration
import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.ast.type.*
import java.util.*

object JavaLanguage : Language() {
    private val parser = JavaParser()

    override val name: String = "Java"
    override val extension: String = "java"

    override fun extractClassDescription(code: String, className: String): ClassOrInterfaceLC {
        val ast = parser.parse(code).result.unwrap()
        requireNotNull(ast) { "Cannot parse given Java file." }

        val type = findType(ast.types, className)
        requireNotNull(type) { "Cannot find given class name." }
        require(type is ClassOrInterfaceDeclaration && !type.isInterface) { "Given object must be a class." }

        return ClassOrInterfaceLC(
            className,
            type.methods.map { it.toCommonMethod() },
            type.typeParameters.map { it.toCommonTypeParameter() }
        )
    }

    private fun findType(roots: List<TypeDeclaration<*>>, typeName: String): TypeDeclaration<*>? {
        for (root in roots) {
            if (root.nameAsString == typeName) {
                return root
            }
            val subtypes = root.findAll(TypeDeclaration::class.java)
            subtypes.firstOrNull { it.nameAsString == typeName }?.let {
                return it
            }
        }
        return null
    }

    override fun constructInterfaceCode(
        interfaceDescription: ClassOrInterfaceLC
    ): String = ClassOrInterfaceDeclaration().apply {
        isInterface = true
        isPublic = true
        name = SimpleName(interfaceDescription.name)
        members = NodeList(interfaceDescription.methods.map { it.toInterfaceMethodDeclaration() })
        typeParameters = NodeList(interfaceDescription.typeParameters.map { it.toTypeParameter() })
    }.toString()

    override fun convertTypeToOtherLanguage(type: TypeLC, otherLanguage: Language): TypeLC = when (otherLanguage) {
        JavaLanguage -> type
        KotlinLanguage -> {
            val newType = when (val jType = type.toType()) {
                is PrimitiveType -> TypeLC(type.name.replaceFirstChar { it.uppercase() })
                is ArrayType -> {
                    val component = convertTypeToOtherLanguage(jType.componentType.toCommonType(), otherLanguage)
                    TypeLC("Array<${component.name}>")
                }
                is VoidType -> TypeLC("Unit")
                else -> type
            }
            TypeLC(newType.name.replace('?', '*'))
        }
    }

    private fun <T> Optional<T>.unwrap(): T? = orElse(null)

    private fun Type.toCommonType(): TypeLC = TypeLC(asString())

    private fun TypeLC.toType(): Type = checkNotNull(parser.parseType(name).result.unwrap()) {
        "Unsupported type in Java: '$name'"
    }

    private fun TypeParameter.toCommonTypeParameter(): TypeParameterLC = TypeParameterLC(
        nameAsString,
        typeBound.map { it.toCommonType() }
    )

    private fun TypeParameterLC.toTypeParameter(): TypeParameter = TypeParameter(
        name,
        NodeList(bound.mapNotNull { it.toType().toClassOrInterfaceType().unwrap() })
    )

    private fun AccessSpecifier.toAccessModifier(): AccessModifierLC = AccessModifierLC(name)

    private fun Parameter.toCommonParameter(): ParameterLC = ParameterLC(nameAsString, type.toCommonType())

    private fun ParameterLC.toParameter(): Parameter = Parameter().also {
        it.name = SimpleName(name)
        it.type = type.toType()
    }

    private fun MethodDeclaration.toCommonMethod(): MethodLC = MethodLC(
        nameAsString,
        type.toCommonType(),
        parameters.map { it.toCommonParameter() },
        typeParameters.map { it.toCommonTypeParameter() },
        accessSpecifier.toAccessModifier()
    )

    private fun MethodLC.toInterfaceMethodDeclaration(): MethodDeclaration = MethodDeclaration().also {
        it.name = SimpleName(name)
        it.type = returnType.toType()
        it.parameters = NodeList(parameters.map { param -> param.toParameter() })
        it.typeParameters = NodeList(typeParameters.map { typeParam -> typeParam.toTypeParameter() })
        it.removeBody()
    }
}
