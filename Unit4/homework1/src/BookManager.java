import com.oocourse.library1.LibraryBookId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BookManager {
    private final HashMap<LibraryBookId, ArrayList<Book>> books;

    public BookManager() {
        books = new HashMap<>();
    }

    public BookManager(Map<LibraryBookId, Integer> inventory) {
        books = new HashMap<>();
        for (Map.Entry<LibraryBookId, Integer> entry : inventory.entrySet()) {
            LibraryBookId bookId = entry.getKey();
            for (int i = 0; i < entry.getValue(); i++) {
                if (!books.containsKey(bookId)) {
                    books.put(bookId, new ArrayList<>());
                }
                books.get(bookId).add(new Book(bookId, i + 1));
            }
        }
    }

    protected HashMap<LibraryBookId, ArrayList<Book>> getBooks() {
        return books;
    }

    protected ArrayList<Book> getBooks(LibraryBookId bookId) {
        return books.get(bookId);
    }

    public boolean hasBook(LibraryBookId bookId) {
        return books.containsKey(bookId) && !books.get(bookId).isEmpty();
    }

    public Book getBook(LibraryBookId bookId) {
        if (hasBook(bookId) && !books.get(bookId).isEmpty()) {
            return books.get(bookId).get(0);
        } else {
            return null;
        }
    }

    public Book getBook(LibraryBookId bookId, Book.State state) {
        if (hasBook(bookId) && !books.get(bookId).isEmpty()) {
            for (Book book : books.get(bookId)) {
                if (book.getState() == state) {
                    return book;
                }
            }
        }
        return null;
    }

    public void addBook(Book book) {
        if (!books.containsKey(book.getBookId())) {
            books.put(book.getBookId(), new ArrayList<>());
        }
        // this assert is for debugging
        assert !books.get(book.getBookId()).contains(book);
        books.get(book.getBookId()).add(book);
    }

    public void removeBook(Book book) {
        books.get(book.getBookId()).remove(book);
    }

    @Override
    public String toString() {
        return books.toString();
    }
}