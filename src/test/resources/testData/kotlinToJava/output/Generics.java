public interface Generics<T extends List<Int>> {

    <First extends Collection<?>, Second extends T> T kek(First first, Second second);

    <O extends T> O kek(T a, T b);
}