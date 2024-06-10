import com.oocourse.library1.LibraryBookId;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BorrowReturnOffice extends BookManager {
    public BorrowReturnOffice() {
        super();
    }

    public List<Book> removeBooksForBookshelf() {
        List<Book> result = new ArrayList<>();
        for (ArrayList<Book> books : getBooks().values()) {
            result.addAll(books.stream()
                    .filter(book -> book.getState() != Book.State.BORROWED)
                    .collect(Collectors.toList()));
            books.removeIf(book -> book.getState() != Book.State.BORROWED);
        }
        return result;
    }

    public Book getBookForReserve(LibraryBookId bookId) {
        return getBook(bookId, Book.State.FREE);
    }

    public boolean hasUserReserveBook(LibraryBookId bookId, User user) {
        return hasBook(bookId) && getBooks(bookId).stream().anyMatch(
            book -> book.getState() == Book.State.RESERVED && user.equals(book.getOwner())
        );
    }

}
