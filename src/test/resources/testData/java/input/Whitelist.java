public class Whitelist {
    public Boolean hello() { return false; }
    public String kek() { return "a"; }
    private String kek(int a) { return "a"; }
    protected String kek(int a, int b) { return ""; }
    private Boolean hello(String a) { return true; }
}