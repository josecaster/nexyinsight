import sr.we.entity.eclipsestore.Database;

public class Test {
    public static void main(String[] args) {
        boolean instance = Database.class.isInstance(new Database());
        System.out.println(instance);
    }
}
