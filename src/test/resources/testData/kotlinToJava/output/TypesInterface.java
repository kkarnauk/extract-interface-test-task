public interface TypesInterface {

    <S> S getS(S def);

    Any getAll(Collection<?> collection);

    <T extends Any> T getNullable(T value);

    <T extends Any> T getNonNullable(T value);

    Unit manyStars(Collection<Collection<Collection<MyClass<?, Collection<?>>>>> c);
}