package org.jetbrains.flcc

import org.jetbrains.flcc.cli.ArgsParser
import org.jetbrains.flcc.cli.Options
import org.jetbrains.flcc.lang.MethodFilter

fun main(args: Array<String>) {
    val options = ArgsParser.parse(args.toList())
    val classCode = options.input.path.toFile().apply { require(isFile) { "'inputPath' must be a file" } }.readText()
    val methodFilter = options.requirements.toFilter()
    val methods = options.input.language.extractMethods(classCode, options.input.name, methodFilter)
    val interfaceCode = options.output.language.constructInterface(options.output.name, methods)
    options.output.path.toFile().writeText(interfaceCode)
}

private fun Options.MethodsRequirements.toFilter(): MethodFilter {
    val whitelist = whitelist?.toSet()
    val blacklist = blacklist?.toSet()
    return { method ->
        method.accessModifier.name.equals(accessModifier, ignoreCase = true) &&
                (whitelist == null || method.name in whitelist) &&
                (blacklist == null || method.name !in blacklist)
    }
}
