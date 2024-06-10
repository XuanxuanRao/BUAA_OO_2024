import com.oocourse.library2.LibraryBookId;
import com.oocourse.library2.LibraryRequest;
import com.oocourse.library2.LibraryMoveInfo;
import com.oocourse.library2.LibraryCommand;
import com.oocourse.library2.LibraryReqCmd;
import com.oocourse.library2.LibraryOpenCmd;
import com.oocourse.library2.LibraryCloseCmd;

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
    private final BookDriftCorner bookDriftCorner;
    private final HashMap<String, User> users;
    private LocalDate today;
    private static final Map<LibraryRequest.Type, BiFunction<LibraryBookId, User, Integer>>
                            behaviors = new HashMap<>();

    public Library(Map<LibraryBookId, Integer> inventory) {
        // register the behavior for each type of request
        behaviors.put(LibraryRequest.Type.BORROWED, this::borrowBook);
        behaviors.put(LibraryRequest.Type.RETURNED, this::returnBook);
        behaviors.put(LibraryRequest.Type.ORDERED, this::orderBook);
        behaviors.put(LibraryRequest.Type.PICKED, this::pickBook);
        behaviors.put(LibraryRequest.Type.QUERIED, this::queryBook);
        behaviors.put(LibraryRequest.Type.DONATED, this::donateBook);
        behaviors.put(LibraryRequest.Type.RENEWED, this::renewBook);

        // initialize the library
        bookShelf = new BookShelf(inventory);
        appointmentOffice = new AppointmentOffice();
        borrowReturnOffice = new BorrowReturnOffice();
        bookDriftCorner = new BookDriftCorner();
        users = new HashMap<>();
    }

    public void run(LibraryCommand input) {
        LocalDate date = input.getDate();
        if (input instanceof LibraryOpenCmd) {
            this.today = date;
            organize();
        } else if (input instanceof LibraryCloseCmd) {
            // do nothing when closing
            Output.print(date, null);
        } else {
            LibraryRequest request = ((LibraryReqCmd) input).getRequest();
            if (!users.containsKey(request.getStudentId())) {
                users.put(request.getStudentId(), new User(request.getStudentId()));
            }
            int result = behaviors.get(request.getType())
                    .apply(request.getBookId(), users.get(request.getStudentId()));
            if (request.getType() == LibraryRequest.Type.QUERIED) {
                Output.print(date, request, result);
            } else if (request.getType() == LibraryRequest.Type.RETURNED) {
                Output.print(input, result == -1 ? "overdue" : "not overdue");
            } else {
                Output.print(input, result != -1);
            }
        }
    }

    private void organize() {
        ArrayList<LibraryMoveInfo> moves = new ArrayList<>();

        // step1: put the books in borrow and return office whose number of returns is less than 2
        //        back to book drift corner
        List<Book> booksToBookDriftCorner = borrowReturnOffice.removeBooksForBookDriftCorner();
        for (Book book : booksToBookDriftCorner) {
            moves.add(new LibraryMoveInfo(book.getBookId(), "bro", "bdc"));
            bookDriftCorner.addBook(book);
        }

        // step2: put the left books in borrow and return office to bookshelf
        List<Book> booksToBookShelf1 = borrowReturnOffice.removeBooksForBookshelf();
        for (Book book : booksToBookShelf1) {
            moves.add(new LibraryMoveInfo(book.getBookId(), "bro", "bs"));
            book.try2SetFormal();
            bookShelf.addBook(book);
        }

        // step3: put the reserved books pass latestPickDate in appointment office to bookshelf
        List<Book> booksToBookShelf2 = appointmentOffice.removeBooksForBookshelf(today);
        for (Book book : booksToBookShelf2) {
            moves.add(new LibraryMoveInfo(book.getBookId(), "ao", "bs"));
            bookShelf.addBook(book);
            book.putBackToBookShelf();
        }

        // step4: move all reserved books in the shelves to the appointment office
        List<Book> booksToAppointmentOffice = bookShelf.removeReservedBooks();
        for (Book book : booksToAppointmentOffice) {
            moves.add(new LibraryMoveInfo(book.getBookId(), "bs", "ao",
                    book.getOwner().getUserId()));
            appointmentOffice.addBook(book);
            book.setLatestPickDate(today);
        }

        Output.print(today, moves);
    }

    private int borrowBook(LibraryBookId bookId, User user) {
        // 从书架取出获取一本书（已从 `bookshelf` 移除）
        Book book = (bookId.isTypeA() || bookId.isTypeB() || bookId.isTypeC())
                ? bookShelf.popBookForBorrow(bookId)
                : bookDriftCorner.popBookForBorrow(bookId);
        if (book != null) {
            // 如果用户可以借阅这本书，用户借阅这本书
            if (user.canBorrow(bookId)) {
                user.borrowBook(book);
                book.setBorrowed(user, today);
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
        return today.isAfter(book.getReturnDeadline()) ? -1 : 1;
    }

    private int orderBook(LibraryBookId bookId, User user) {
        if (!user.canOrder(bookId)) {
            return -1;
        }
        user.orderBook(bookId);
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
        return appointmentOffice.popBookForPick(bookId, user, today) != null ? 1 : -1;
    }

    private int queryBook(LibraryBookId bookId, User user) {
        return bookId.isTypeA() || bookId.isTypeB() || bookId.isTypeC() ?
                bookShelf.numberOfAvailableBooks(bookId) :
                bookDriftCorner.hasBook(bookId) ? 1 : 0;
    }

    /**
     * Donate a book to book drift corner.
     * @param bookId  the id of donated book (with U)
     * @param user    the user who donate the book
     * @return return 1 if success(ensure success)
     */
    private int donateBook(LibraryBookId bookId, User user) {
        Book book = new Book(bookId);
        bookDriftCorner.addBook(book);
        return 1;   // ensure success
    }

    /**
     * User try to renew a book. If success, the borrowing period will be extended for 30 days.
     * @param bookId the id of the book to be renewed
     * @param user   the user who want to renew the book
     * @return return -1 if not within the first 5 days of the book return deadline,
     *          return 1 if success
     */
    private int renewBook(LibraryBookId bookId, User user) {
        // todo: If some one ordered this book **and** there is no copy in bookshelf, failed
        // 若存在任意一位用户对该书正在生效的预约（从预约成功开始，直到取书成功预约完成前，或送书后一直未取书导致预约失效前）
        // 且该书无在架余本，则续借失败
        if (!bookShelf.hasBook(bookId) &&
                users.values().stream().anyMatch(u -> u.hasOrdered(bookId))) {
            return -1;
        }
        return user.try2RenewBook(bookId, today) ? 1 : -1;
    }

    @Override
    public String toString() {
        return "\t\t\tLibrary information\t\t\t\n" +
                "BookShelf: " + bookShelf.getBooks().values() + "\n" +
                "AppointmentOffice: " + appointmentOffice.getBooks().values() + "\n" +
                "BorrowReturnOffice: " + borrowReturnOffice.getBooks().values() + "\n" +
                "BookDriftCorner: " + bookDriftCorner.getBooks().values() + "\n" +
                "Users: " + users.values() + "\n";
    }
}
