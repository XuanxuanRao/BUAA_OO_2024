import com.oocourse.library2.LibraryScanner;
import com.oocourse.library2.LibrarySystem;
import com.oocourse.library2.LibraryCommand;

public class Main {
    public static void main(String[] args) {
        LibraryScanner sc = LibrarySystem.SCANNER;
        Library library = new Library(sc.getInventory());
        for (LibraryCommand input = sc.nextCommand(); input != null; input = sc.nextCommand()) {
            library.run(input);
            //System.out.println(library);
        }
    }
}