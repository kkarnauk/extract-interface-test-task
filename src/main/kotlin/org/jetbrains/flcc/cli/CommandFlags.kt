package org.jetbrains.flcc.cli

import org.jetbrains.flcc.lang.Language
import java.nio.file.Path

/**
 * Represents a command line flag.
 * @param value current value of this flag.
 * @param T type of flag value
 */
sealed class CommandFlag<T>(var value: T) {
    /**
     * Converts a string representation of a given flag value to a value of the type ans sets it.
     */
    abstract fun setValue(stringValue: String)
}

class StringFlag : CommandFlag<String?>(null) {
    override fun setValue(stringValue: String) = run { value = stringValue }
}

class ListFlag : CommandFlag<List<String>?>(null) {
    override fun setValue(stringValue: String) = stringValue.run {
        require(first() == '[') { "Incorrect format for list." }
        require(last() == ']') { "Incorrect format for list." }
        value = substring(1, length - 1).split(',').map { it.trim() }
    }
}

class PathFlag : CommandFlag<Path?>(null) {
    override fun setValue(stringValue: String) = run { value = Path.of(stringValue).toAbsolutePath() }
}

class LanguageFlag : CommandFlag<Language?>(null) {
    override fun setValue(stringValue: String) {
        value = requireNotNull(Language.forName(stringValue)) {
            "Cannot find a language by name '$stringValue'."
        }
    }
}
