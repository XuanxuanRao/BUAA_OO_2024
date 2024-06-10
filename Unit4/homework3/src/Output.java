import com.oocourse.library3.LibraryMoveInfo;
import com.oocourse.library3.LibraryPrinter;
import com.oocourse.library3.LibraryRequest;
import com.oocourse.library3.LibrarySystem;
import com.oocourse.library3.LibraryCommand;

import java.time.LocalDate;
import java.util.ArrayList;

public class Output {
    private static final LibraryPrinter printer = LibrarySystem.PRINTER;

    public static void print(LocalDate date, LibraryRequest request, int count) {
        printer.info(date, request.getBookId(), count);
    }

    public static void print(LocalDate date, User user) {
        printer.info(date, user.getUserId(), user.getCreditScore());
    }

    public static void print(LibraryCommand cmd, boolean result) {
        if (result) {
            printer.accept(cmd);
        } else {
            printer.reject(cmd);
        }
    }

    public static void print(LibraryCommand cmd, String s) {
        printer.accept(cmd, s);
    }

    public static void print(LocalDate date, ArrayList<LibraryMoveInfo> moves) {
        if (moves == null) {
            printer.move(date);
        } else {
            printer.move(date, moves);
        }
    }
}
