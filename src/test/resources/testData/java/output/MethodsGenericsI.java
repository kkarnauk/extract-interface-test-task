public interface MethodsGenericsI<T> {

    <F extends T> int one(int a);

    <S extends T, Q extends S> String three(Boolean bbb);
}