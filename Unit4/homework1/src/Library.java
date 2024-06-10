import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryCommand;
import com.oocourse.library1.LibraryMoveInfo;
import com.oocourse.library1.LibraryRequest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class Library {
    private final BookShelf bookShelf;
    private final AppointmentOffice appointmentOffice;
    private final BorrowReturnOffice borrowReturnOffice;
    private final HashMap<String, User> users;
    private LocalDate date;
    private static final Map<LibraryRequest.Type, BiFunction<LibraryBookId, User, Integer>>
                            behaviors = new HashMap<>();

    public Library(Map<LibraryBookId, Integer> inventory) {
        // register the behavior for each type of request
        behaviors.put(LibraryRequest.Type.BORROWED, this::borrowBook);
        behaviors.put(LibraryRequest.Type.RETURNED, this::returnBook);
        behaviors.put(LibraryRequest.Type.ORDERED, this::orderBook);
        behaviors.put(LibraryRequest.Type.PICKED, this::pickBook);
        behaviors.put(LibraryRequest.Type.QUERIED, this::queryBook);

        // initialize the library
        bookShelf = new BookShelf(inventory);
        appointmentOffice = new AppointmentOffice();
        borrowReturnOffice = new BorrowReturnOffice();
        users = new HashMap<>();
    }

    public void run(LibraryCommand<?> input) {
        LocalDate date = input.getDate();
        if (input.getCmd() instanceof LibraryRequest) {
            LibraryRequest request = (LibraryRequest) input.getCmd();
            if (!users.containsKey(request.getStudentId())) {
                users.put(request.getStudentId(), new User(request.getStudentId()));
            }
            int result = behaviors.get(request.getType())
                                    .apply(request.getBookId(), users.get(request.getStudentId()));
            if (request.getType() == LibraryRequest.Type.QUERIED) {
                Output.print(date, request, result);
            } else {
                Output.print(date, request, result != -1);
            }
        } else if (input.getCmd().equals("OPEN")) {
            this.date = date;
            organize();
        } else {
            Output.print(date, null);
        }
    }

    private void organize() {
        ArrayList<LibraryMoveInfo> moves = new ArrayList<>();

        // step1: put the books in return and borrow office back to bookshelf
        List<Book> booksToBookShelf1 = borrowReturnOffice.removeBooksForBookshelf();
        for (Book book : booksToBookShelf1) {
            moves.add(new LibraryMoveInfo(book.getBookId(), "bro", "bs"));
            bookShelf.addBook(book);
        }

        // step2: put the reserved books pass latestPickDate in appointment office to bookshelf,
        //        the reserved books in latestPickDate stay in appointment office
        List<Book> booksToBookShelf2 = appointmentOffice.removeBooksForBookshelf(date);
        for (Book book : booksToBookShelf2) {
            moves.add(new LibraryMoveInfo(book.getBookId(), "ao", "bs"));
            bookShelf.addBook(book);
            book.putBackToBookShelf();
        }

        // step3: move all reserved books in the shelves to the appointment office
        List<Book> booksToAppointmentOffice = bookShelf.removeReservedBooks();
        for (Book book : booksToAppointmentOffice) {
            moves.add(new LibraryMoveInfo(book.getBookId(), "bs", "ao",
                    book.getOwner().getUserId()));
            appointmentOffice.addBook(book);
            book.setLatestPickDate(date);
        }

        Output.print(date, moves);
    }

    private int borrowBook(LibraryBookId bookId, User user) {
        // 从书架取出获取一本书（已从 `bookshelf` 移除）
        Book book = bookShelf.popBookForBorrow(bookId);
        if (book != null) {
            // 如果用户可以借阅这本书，用户借阅这本书
            if (user.canBorrowOrOrder(bookId)) {
                user.borrowBook(book);
                book.setBorrowed(user);
                return 1;
            }
            // 如果用户不能借阅这本书，书留在 `borrowReturnOffice`
            else {
                borrowReturnOffice.addBook(book);
                return -1;
            }
        } else {
            return -1;
        }
    }

    private int returnBook(LibraryBookId bookId, User user) {
        Book book = user.returnBook(bookId);
        borrowReturnOffice.addBook(book);
        book.setReturned();
        return 1;
    }

    private int orderBook(LibraryBookId bookId, User user) {
        if (!user.canBorrowOrOrder(bookId)) {
            return -1;
        }

        // 避免重复为一个用户预约（送往 appointment office）同一本书
        if (!bookShelf.hasUserReserveBook(bookId, user)) {
            // 从书架获得一本书 （没有从 `bookshelf` 移除）
            Book book = bookShelf.getBookForReserve(bookId);
            if (book != null) {
                book.setReserved(user);
                return 1;
            }
        }

        if (!borrowReturnOffice.hasUserReserveBook(bookId, user)) {
            Book book = borrowReturnOffice.getBookForReserve(bookId);
            if (book != null) {
                book.setReserved(user);
                return 1;
            }
        }
        return 1;
    }

    private int pickBook(LibraryBookId bookId, User user) {
        if (appointmentOffice.popBookForPick(bookId, user, date) != null) {
            return 1;
        } else {
            return -1;
        }
    }

    private int queryBook(LibraryBookId bookId, User user) {
        return bookShelf.numberOfAvailableBooks(bookId);
    }

    @Override
    public String toString() {
        return "\t\t\tLibrary information\t\t\t\n" +
                "BookShelf: " + bookShelf.getBooks().values() + "\n" +
                "AppointmentOffice: " + appointmentOffice.getBooks().values() + "\n" +
                "BorrowReturnOffice: " + borrowReturnOffice.getBooks().values() + "\n" +
                "Users: " + users.values() + "\n";
    }
}
