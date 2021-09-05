package org.jetbrains.flcc.cli

import java.lang.IllegalArgumentException
import java.nio.file.Path
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

data class CliOptions(
    val className: String?,
    val interfaceName: String?,
    val methodsWhitelist: List<String>,
    val methodsBlacklist: List<String>,
    val accessModifier: String,
    val inputPath: Path,
    val outputPath: Path?
) {
    class Builder {
        private var className: ClassName = ClassName()
        private var interfaceName: InterfaceName = InterfaceName()
        private var methodsWhitelist: Whitelist = Whitelist()
        private var methodsBlacklist: Blacklist = Blacklist()
        private var accessModifier: AccessModifier = AccessModifier()
        private var inputPath: InputPath = InputPath()
        private var outputPath: OutputPath = OutputPath()

        fun set(name: String, value: String): Builder {
            for (prop in Builder::class.memberProperties) {
                if (prop.name.equals(name, ignoreCase = true)) {
                    prop.isAccessible = true
                    val flag = requireNotNull(prop.get(this) as? Flag<*>)
                    flag.setValue(value)
                    return this
                }
            }

            throw IllegalArgumentException("No property with name '$name'.")
        }

        fun build(): CliOptions {

            return CliOptions(
                className.value,
                interfaceName.value,
                methodsWhitelist.value,
                methodsBlacklist.value,
                accessModifier.value,
                inputPath.value ?: throw IllegalStateException("Property 'inputPath' must be initialized."),
                outputPath.value
            )
        }

        private sealed class Flag<T>(var value: T) {
            val name: String = this::class::simpleName.name
            abstract fun setValue(stringValue: String)
        }

        private class ClassName : Flag<String?>(null) {
            override fun setValue(stringValue: String) = run { value = stringValue }
        }

        private class InterfaceName : Flag<String?>(null) {
            override fun setValue(stringValue: String) = run { value = stringValue }
        }

        private class Whitelist: Flag<List<String>>(emptyList()) {
            override fun setValue(stringValue: String) = run { value = stringValue.toListOfString() }
        }

        private class Blacklist: Flag<List<String>>(emptyList()) {
            override fun setValue(stringValue: String) = run { value = stringValue.toListOfString() }
        }

        private class AccessModifier: Flag<String>("public") {
            override fun setValue(stringValue: String) = run { value = stringValue }
        }

        private class InputPath: Flag<Path?>(null) {
            override fun setValue(stringValue: String) = run { value = Path.of(stringValue) }
        }

        private class OutputPath: Flag<Path?>(null) {
            override fun setValue(stringValue: String) = run { value = Path.of(stringValue) }
        }

        companion object {
            private fun String.toListOfString(): List<String> {
                check(first() == '[') { "Incorrect format for list." }
                check(last() == ']') { "Incorrect format for list." }
                return substring(1, length - 1).split(',').onEach { it.trim() }
            }
        }
    }
}
