public interface ClassGenerics<First extends Integer, Second extends List<Integer>> {

    Second getSecond();

    First merge(First a, Second b);
}