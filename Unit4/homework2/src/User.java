import com.oocourse.library2.LibraryBookId;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class User {
    private final String id;
    private final ArrayList<Book> books;
    private final HashMap<LibraryBookId, Integer> orderedBooks;

    public User(String id) {
        this.id = id;
        books = new ArrayList<>();
        orderedBooks = new HashMap<>();
    }

    public String getUserId() {
        return id;
    }

    public void borrowBook(Book book) {
        // todo: this is for development, remove it when submitting
        //assert canBorrow(book.getBookId());
        books.add(book);
    }

    public void pickBook(Book book) {
        // todo: this is for development, remove it when submitting
        //assert canBorrow(book.getBookId());
        books.add(book);
        orderedBooks.computeIfPresent(
            book.getBookId(),
            (id, count) -> count == 1 ? null : count - 1
        );
    }

    public boolean hasOrdered(LibraryBookId bookId) {
        return orderedBooks.getOrDefault(bookId, 0) > 0;
    }

    public void orderBook(LibraryBookId bookId) {
        assert canOrder(bookId);
        orderedBooks.put(bookId, orderedBooks.getOrDefault(bookId, 0) + 1);
    }

    public boolean canOrder(LibraryBookId bookId) {
        if (bookId.isTypeA() || bookId.isTypeAU() || bookId.isTypeBU() || bookId.isTypeCU()) {
            return false;
        } else if (bookId.isTypeB()) {
            return books.stream().noneMatch(book -> book.getBookId().isTypeB());
        } else {
            return books.stream().noneMatch(book -> book.getBookId().equals(bookId));
        }
    }

    public boolean canBorrow(LibraryBookId bookId) {
        if (bookId.isTypeA() || bookId.isTypeAU()) {
            return false;
        } else if (bookId.isTypeB()) {
            return books.stream().noneMatch(book -> book.getBookId().isTypeB());
        } else if (bookId.isTypeBU()) {
            return books.stream().noneMatch(book -> book.getBookId().isTypeBU());
        } else {
            return books.stream().noneMatch(book -> book.getBookId().equals(bookId));
        }
    }

    public Book returnBook(LibraryBookId bookId) {
        Book res = null;
        Iterator<Book> iterator = books.iterator();
        while (iterator.hasNext()) {
            Book book = iterator.next();
            if (book.getBookId().equals(bookId)) {
                res = book;
                iterator.remove();
                break;
            }
        }
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
        return id + ": " + books;
    }

    public boolean try2RenewBook(LibraryBookId bookId, LocalDate today) {
        if (bookId.isTypeAU() || bookId.isTypeBU() || bookId.isTypeCU()) {
            return false;
        }
        Book book = books.stream().filter(b -> b.getBookId().equals(bookId))
                        .findFirst().orElse(null);
        assert book != null;
        return book.try2Renew(today);
    }

    public void removeOrder(LibraryBookId bookId) {
        assert hasOrdered(bookId);
        orderedBooks.computeIfPresent(bookId, (id, count) -> count == 1 ? null : count - 1);
    }
}
