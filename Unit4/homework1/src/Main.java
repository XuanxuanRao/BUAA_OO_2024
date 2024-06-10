import com.oocourse.library1.LibraryScanner;
import com.oocourse.library1.LibrarySystem;
import com.oocourse.library1.LibraryCommand;

public class Main {
    public static void main(String[] args) {
        LibraryScanner sc = LibrarySystem.SCANNER;
        Library library = new Library(sc.getInventory());
        LibraryCommand<?> input = sc.nextCommand();
        while (input != null) {
            library.run(input);
            input = sc.nextCommand();
        }
    }
}