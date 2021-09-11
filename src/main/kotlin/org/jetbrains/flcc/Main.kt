package org.jetbrains.flcc

import org.jetbrains.flcc.cli.ArgsParser
import org.jetbrains.flcc.cli.Options
import org.jetbrains.flcc.lang.ClassOrInterfaceLC
import org.jetbrains.flcc.lang.MethodLC

fun main(args: Array<String>) {
    val options = ArgsParser.parse(args.toList())
    val classCode = options.input.path.toFile().apply { require(isFile) { "'inputPath' must be a file" } }.readText()
    val classDescription = options.input.language.extractClassDescription(classCode, options.input.name)
    val interfaceMethods = filterMethods(classDescription.methods, options.requirements)
    val interfaceDescription = ClassOrInterfaceLC(options.output.name, interfaceMethods)
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
