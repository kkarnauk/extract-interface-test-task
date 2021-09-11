public class DeepNestedClass {
    public void outer() {}
    public static class Nested1 {
        public void nested1() {}
        public static class Nested2 {
            public void nested2() {}
            public static class Nested3 {
                public void nested3() {}
                public static class Nested4 {
                    public void nested4() {}
                    public static class Nested5 {
                        public void nested5() {}
                    }
                }
            }
        }
    }
}