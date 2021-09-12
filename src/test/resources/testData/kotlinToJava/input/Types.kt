class Types {
    fun <S> getS(def: S): S = def

    fun getAll(collection: Collection<*>): Any? = collection.first()

    fun <T : Any> getNullable(value: T) : T? = value

    fun <T : Any> getNonNullable(value: T?) : T = value!!

    fun manyStars(c: Collection<Collection<Collection<MyClass<*, Collection<*>>>>>) = Unit
}