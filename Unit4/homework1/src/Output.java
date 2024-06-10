import com.oocourse.library1.LibraryMoveInfo;
import com.oocourse.library1.LibraryPrinter;
import com.oocourse.library1.LibraryRequest;
import com.oocourse.library1.LibrarySystem;

import java.time.LocalDate;
import java.util.ArrayList;

public class Output {
    private static final LibraryPrinter printer = LibrarySystem.PRINTER;

    public static void print(LocalDate date, LibraryRequest request, int count) {
        printer.info(date, request.getBookId(), count);
    }

    public static void print(LocalDate date, LibraryRequest request, boolean result) {
        if (result) {
            printer.accept(date, request);
        } else {
            printer.reject(date, request);
        }
    }

    public static void print(LocalDate date, ArrayList<LibraryMoveInfo> moves) {
        if (moves == null) {
            printer.move(date);
        } else {
            printer.move(date, moves);
        }
    }
}
