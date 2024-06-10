import com.oocourse.spec2.main.Runner;

public class Main {
    public static void main(String[] args) throws Exception {
        new Runner(MyPerson.class, MyNetwork.class, MyTag.class).run();
    }
}