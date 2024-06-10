import com.oocourse.library2.LibraryBookId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class BookShelf extends BookManager {
    public BookShelf(Map<LibraryBookId, Integer> inventory) {
        super(inventory);
    }

    /**
     * Get a book that can be borrowed with the given bookId and then remove it from the bookshelf.
     * If there are many books with given bookId,
     * return and remove the first book whose state is {@code FREE}.
     * Note: If {@code bookId.isTypeA()}, we do nothing and return null.
     * @param bookId the bookId want to borrow
     * @return the book with the given bookId
     */
    public Book popBookForBorrow(LibraryBookId bookId) {
        if (bookId.isTypeA()) {
            return null;
        }
        Book book = Optional.ofNullable(getBook(bookId, Book.State.FREE))
                        .orElseGet(() -> getBook(bookId));
        if (book != null) {
            removeBook(book);
        }
        return book;
    }

    public Book getBookForReserve(LibraryBookId bookId) {
        return getBook(bookId, Book.State.FREE);
    }

    public int numberOfAvailableBooks(LibraryBookId bookId) {
        return getBooks(bookId).size();
    }

    public boolean hasUserReserveBook(LibraryBookId bookId, User user) {
        return hasBook(bookId) && getBooks(bookId).stream().anyMatch(
            book -> book.getState() == Book.State.RESERVED && user.equals(book.getOwner())
        );
    }

    public List<Book> removeReservedBooks() {
        List<Book> result = new ArrayList<>();
        for (ArrayList<Book> books : getBooks().values()) {
            result.addAll(books.stream()
                    .filter(book -> book.getState() == Book.State.RESERVED)
                    .collect(Collectors.toList()));
            books.removeIf(book -> book.getState() == Book.State.RESERVED);
        }
        return result;
    }
}
