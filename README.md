# Extract Interface

It's a test task for the Fall 2021 Practice in JetBrains of the team Full Line Code Completion.

The main purpose of this project is to extract methods from a class with specific information, like a name or an access
modifier. What is supported now will be described below.

### Supported languages

Now there are two languages: **Kotlin and Java**. However, you cannot construct an interface in Kotlin, but you can extract
methods from it. Also, it's possible to extract methods from Kotlin and construct an interface in Java.

### Supported options

You can pass some options for extracting methods in the command line:
* `-inputPath`: a path to an input class. You **MUST** pass this option.
* `-outputPath`: a path to an output interface. By default, it's `path/to/input/directory/$interfaceName.extension`.
* `-inputLanguage`: a language for an input class. By default, it's `Java`. **Ignores case**.
* `-outputLanguage`: a language for an output interface. By default, it's `Java`. **Ignores case**.
* `-className`: a name for an input class. By default, it's a name of an input file (without extension).
* `-interfaceName`: a name for an output interface. By default, it's `classnameInterface`.
* `-whitelist`: if a method name is in this list, it is extracted. By default, all methods are in the list.
  You can check out the list format after this options enumeration.
* `-blacklist`: if a method name is in this list, it isn't extracted. By default, all methods aren't in the list.
* `-accessModifier`: only methods with this access modifier are extracted. By default, it's `public`. **Ignores case**.

It's the list format: `[first, second, third, fourth]`. There are no restrictions for spaces number. 
**Important**: pass a list in `""`.

You must pass options as pairs of a flag and its value.

### Example of a configuration

It's an example of program arguments in IntelliJ-IDEA for the `Main`:
```
-inputPath test/Repository.kt -accessModifier private -inputLanguage kotlin -outputLanguage java -whitelist="[get, remove]"
```

### TODO

* Add tests
* Constructing a Kotlin interface
* Support generics
* Come up with types that exist in Java and don't in Kotlin, and vice versa (for example, `int` and `Int`)
