package org.jetbrains.flcc

import org.jetbrains.flcc.cli.ArgsParser
import org.jetbrains.flcc.cli.Options
import org.jetbrains.flcc.lang.MethodSignature

fun main(args: Array<String>) {
    val options = ArgsParser.parse(args.toList())
    val classCode = options.input.path.toFile().apply { require(isFile) { "'inputPath' must be a file" } }.readText()
    val methods = options.input.language.extractAllMethods(classCode, options.input.name)
    val interfaceMethods = filterMethods(methods, options.requirements)
    val interfaceCode = options.output.language.constructInterface(options.output.name, interfaceMethods)
    options.output.path.toFile().writeText(interfaceCode)
}

private fun filterMethods(
    methods: List<MethodSignature>,
    requirements: Options.MethodsRequirements
): List<MethodSignature> {
    val whitelist = requirements.whitelist?.toSet()
    val blacklist = requirements.blacklist?.toSet()
    val accessModifier = requirements.accessModifier
    return methods.filter { method ->
        method.accessModifier.name.equals(accessModifier, ignoreCase = true) &&
                (whitelist == null || method.name in whitelist) &&
                (blacklist == null || method.name !in blacklist)
    }
}
