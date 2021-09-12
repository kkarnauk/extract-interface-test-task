class Generics<T : List<Int>> {
    fun <First : Collection<*>, Second : T> kek(first: First, second: Second): T {
        TODO()
    }

    fun <O : T> kek(a: T, b: T): O = TODO()
}