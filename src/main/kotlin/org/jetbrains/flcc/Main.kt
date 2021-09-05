package org.jetbrains.flcc

import org.jetbrains.flcc.cli.ArgsParser

fun main(args: Array<String>) {
    val cliOptions = ArgsParser.parse(args.toList())
    println(cliOptions)
    // TODO options -> methods
    // TODO methods -> interface
    // TODO interface -> dump
}
