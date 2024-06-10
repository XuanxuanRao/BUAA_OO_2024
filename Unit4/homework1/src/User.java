import com.oocourse.library1.LibraryBookId;

import java.util.HashMap;

public class User {
    private final String id;
    private Book ownedBBook;                                // can only have one B book
    private final HashMap<LibraryBookId, Book> ownedCBooks;

    public User(String id) {
        this.id = id;
        ownedCBooks = new HashMap<>();
        ownedBBook = null;
    }

    public String getUserId() {
        return id;
    }

    public void borrowBook(Book book) {
        assert canBorrowOrOrder(book.getBookId());
        if (book.getBookId().isTypeB()) {
            ownedBBook = book;
        } else {
            ownedCBooks.put(book.getBookId(), book);
        }
    }

    public boolean canBorrowOrOrder(LibraryBookId bookId) {
        if (bookId.isTypeA()) {
            return false;
        } else if (bookId.isTypeB()) {
            return ownedBBook == null;
        } else {
            return !ownedCBooks.containsKey(bookId);
        }
    }

    public Book returnBook(LibraryBookId bookId) {
        Book res = null;
        if (bookId.isTypeB()) {
            res = ownedBBook;
            ownedBBook = null;
        } else if (bookId.isTypeC()) {
            res = ownedCBooks.remove(bookId);
        }
        assert res != null;     // for debugging
        return res;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof User) {
            return id.equals(((User) obj).getUserId());
        }
        return false;
    }

    @Override
    public String toString() {
        return "[" + id + " borrow: " + ownedBBook + " + " + ownedCBooks + "]";
    }

}
