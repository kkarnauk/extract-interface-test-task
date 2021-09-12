package org.jetbrains.flcc

import org.jetbrains.flcc.cli.ArgsParser
import org.jetbrains.flcc.cli.Options
import org.jetbrains.flcc.lang.*

fun main(args: Array<String>) = extractInfoAndConstructInterface(ArgsParser.parse(args.toList()))

internal fun extractInfoAndConstructInterface(options: Options) {
    val classCode = options.input.path.toFile().apply { require(isFile) { "'inputPath' must be a file" } }.readText()
    val classDescription = options.input.language.extractClassDescription(classCode, options.input.name)
        .toOtherLanguage(options.input.language, options.output.language)

    val filteredMethods = filterMethods(classDescription.methods, options.requirements)
    val interfaceDescription = ClassOrInterfaceLC(
        options.output.name,
        filteredMethods,
        classDescription.typeParameters
    )
    val interfaceCode = options.output.language.constructInterfaceCode(interfaceDescription)
    options.output.path.toFile().writeText(interfaceCode)
}

private fun filterMethods(
    methods: List<MethodLC>,
    requirements: Options.MethodsRequirements
): List<MethodLC> {
    val whitelist = requirements.whitelist?.toSet()
    val blacklist = requirements.blacklist?.toSet()
    val accessModifier = requirements.accessModifier
    return methods.filter { method ->
        method.accessModifier.name.equals(accessModifier, ignoreCase = true) &&
                (whitelist == null || method.name in whitelist) &&
                (blacklist == null || method.name !in blacklist)
    }
}

private fun ClassOrInterfaceLC.toOtherLanguage(input: Language, output: Language): ClassOrInterfaceLC {
    fun TypeLC.convert(): TypeLC = input.convertTypeToOtherLanguage(this, output)

    fun ParameterLC.convert(): ParameterLC = ParameterLC(name, type.convert())

    fun TypeParameterLC.convert(): TypeParameterLC = TypeParameterLC(name, bound.map { it.convert() })

    fun MethodLC.convert(): MethodLC = MethodLC(
        name,
        returnType.convert(),
        parameters.map { it.convert() },
        typeParameters.map { it.convert() },
        accessModifier
    )

    return ClassOrInterfaceLC(
        name,
        methods.map { it.convert() },
        typeParameters.map { it.convert() }
    )
}
