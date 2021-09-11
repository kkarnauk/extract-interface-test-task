public class MethodsGenerics<T> {
    private <F extends T> int one(int a) { return 0; }

    private <S extends Integer> int two(String str) { return 0; }

    private <S extends T, Q extends S> String three(Boolean bbb) { return null; }
}