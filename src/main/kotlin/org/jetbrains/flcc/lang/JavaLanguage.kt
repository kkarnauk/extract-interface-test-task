package org.jetbrains.flcc.lang

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.body.Parameter
import com.github.javaparser.ast.body.TypeDeclaration
import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.ast.type.Type
import java.util.*

object JavaLanguage : Language() {
    private val parser = JavaParser()

    override val name: String = "Java"
    override val extension: String = "java"

    override fun extractMethods(
        code: String,
        className: String,
        methodFilter: (MethodDeclaration) -> Boolean
    ): List<MethodSignature> {
        val ast = parser.parse(code).result.unwrap()
        requireNotNull(ast) { "Cannot parse given Java file." }

        val type = findType(ast.types, className)
        requireNotNull(type) { "Cannot find given class name." }

        return type.methods.filter(methodFilter).map { it.toMethodSignature() }
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

    override fun constructInterface(
        interfaceName: String,
        methods: List<MethodSignature>
    ): String = ClassOrInterfaceDeclaration().apply {
        isInterface = true
        isPublic = true
        name = SimpleName(interfaceName)
        members = NodeList(methods.map { it.toInterfaceMethodDeclaration() })
    }.toString()


    private fun <T> Optional<T>.unwrap(): T? = orElse(null)

    private fun Type.toCommonType(): MethodSignature.Type = MethodSignature.Type(asString())

    private fun MethodSignature.Type.toType(): Type =
        checkNotNull(parser.parseType(name).result.unwrap())

    private fun Parameter.toCommonParameter(): MethodSignature.Parameter =
        MethodSignature.Parameter(nameAsString, type.toCommonType())

    private fun MethodSignature.Parameter.toParameter(): Parameter = Parameter().also {
        it.name = SimpleName(name)
        it.type = type.toType()
    }

    private fun MethodDeclaration.toMethodSignature(): MethodSignature = MethodSignature(
        nameAsString,
        type.toCommonType(),
        parameters.map { it.toCommonParameter() }
    )

    private fun MethodSignature.toInterfaceMethodDeclaration(): MethodDeclaration = MethodDeclaration().also {
        it.name = SimpleName(name)
        it.type = checkNotNull(parser.parseType(returnType.name).result.unwrap())
        it.parameters = NodeList(parameters.map { param -> param.toParameter() })
        it.removeBody()
    }
}
