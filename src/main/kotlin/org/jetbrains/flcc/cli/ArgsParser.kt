package org.jetbrains.flcc.cli

object ArgsParser {
    fun parse(args: List<String>): Options {
        val builder = Options.Builder()
        for (i in args.indices step 2) {
            require(args[i].isFlag()) {
                "Incorrect arguments format: every second argument must be a flag name."
            }
            require(i < args.size - 1) { "Each flag must be initialized." }
            val flag = args[i].flagName()
            val value = args[i + 1]
            builder.set(flag, value)
        }

        return builder.build()
    }

    private fun String.isFlag() = startsWith("-")
    private fun String.flagName(): String = apply { require(isFlag()) }.substring(1)
}
