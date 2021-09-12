package org.jetbrains.flcc.lang

import kotlinx.ast.common.AstResult
import kotlinx.ast.common.AstSource
import kotlinx.ast.common.ast.Ast
import kotlinx.ast.common.ast.AstNode
import kotlinx.ast.common.klass.*
import kotlinx.ast.common.map.TreeMapResultFactory
import kotlinx.ast.grammar.kotlin.common.summary
import kotlinx.ast.grammar.kotlin.target.antlr.kotlin.KotlinGrammarAntlrKotlinParser

@Suppress("unused")
object KotlinLanguage : Language() {
    override val name get(): String = "Kotlin"
    override val extension get(): String = "kt"

    override fun extractClassDescription(code: String, className: String): ClassOrInterfaceLC {
        val source = AstSource.String("", code)
        val methods = mutableListOf<MethodLC>()
        val typeParameters = mutableListOf<TypeParameterLC>()
        KotlinGrammarAntlrKotlinParser.parseKotlinFile(source).summaryOnSuccess { topLevelDecls ->
            val klass = topLevelDecls.filterIsInstance<KlassDeclaration>().firstOrNull {
                it.isClass && it.identifier?.rawName == className
            }
            requireNotNull(klass) { "Cannot find given class name." }

            val body = klass.childrenOrEmpty.firstOrNull { it.isClassBody }
            val decls = body?.childrenOrEmpty?.filterIsInstance<KlassDeclaration>()
            decls?.filter { it.isFun }?.mapNotNull { it.toCommonMethod() }?.forEach(methods::add)

            klass.typeParameters.forEach { typeParameters.add(it.toCommonTypeParameter()) }
        }

        return ClassOrInterfaceLC(className, methods, typeParameters)
    }

    override fun convertTypeToOtherLanguage(type: TypeLC, otherLanguage: Language) = when (otherLanguage) {
        KotlinLanguage -> type
        JavaLanguage -> {
            TypeLC(type.name.filter { it != '?' }.replace('*', '?'))
        }
    }

    private val Ast.childrenOrEmpty get(): List<Ast> = (this as? AstNode)?.children ?: emptyList()

    private fun Ast.summaryOnSuccess(
        callback: TreeMapResultFactory<Unit>.(List<Ast>) -> Unit
    ): AstResult<Unit, List<Ast>> = summary(attachRawAst = false).onSuccess(callback).onFailure {
        throw IllegalArgumentException("Cannot parse Kotlin file:\n$it")
    }

    private fun KlassDeclaration.toCommonMethod(): MethodLC? {
        check(isFun)
        val parameters = parameter.mapNotNull { param ->
            param.toName()?.let { name ->
                ParameterLC(name, param.type.toCommonType())
            }
        }
        return toName()?.let { name ->
            MethodLC(
                name,
                type.toCommonType(),
                parameters,
                typeParameters.map { it.toCommonTypeParameter() },
                toCommonAccessModifier()
            )
        }
    }

    private fun List<KlassIdentifier>.toCommonType(): TypeLC =
        TypeLC(this.identifierNameOrNull() ?: "Unit")

    private fun KlassIdentifier.toCommonType(): TypeLC = TypeLC(rawName)

    private fun KlassTypeParameter.toCommonTypeParameter(): TypeParameterLC =
        TypeParameterLC(generic.identifier, base.map { it.toCommonType() })

    private fun KlassDeclaration.toName(): String? = identifier?.rawName

    private fun KlassDeclaration.toCommonAccessModifier(): AccessModifierLC =
        AccessModifierLC(modifiers.firstOrNull { it.isVisibility }?.modifier ?: "public")

    private val KlassDeclaration.isClass get(): Boolean = keyword == "class"
    private val KlassDeclaration.isFun get(): Boolean = keyword == "fun"
    private val Ast.isClassBody get(): Boolean = description == "classBody"
    private val KlassModifier.isVisibility get(): Boolean = group.group == "visibilityModifier"
}
