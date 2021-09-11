public class ClassGenerics<First extends Integer, Second extends List<Integer>> {
    public First getFirst() { return null; }
    protected Second getSecond() { return null; }

    protected First merge(First a, Second b) {
        return null;
    }
}