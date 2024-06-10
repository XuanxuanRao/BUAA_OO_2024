import com.oocourse.library3.LibraryBookId;
import com.oocourse.library3.annotation.SendMessage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class User {
    private final String id;
    private final ArrayList<Book> borrowedBooks;
    private final HashSet<LibraryBookId> orderedBooks;
    private int creditScore;
    private static final int MAX_CREDIT_SCORE = 20;

    public User(String id) {
        this.id = id;
        borrowedBooks = new ArrayList<>();
        orderedBooks = new HashSet<>();
        creditScore = 10;
    }

    public String getUserId() {
        return id;
    }

    public void borrowBook(Book book) {
        // todo: this is for development, remove it when submitting
        // assert canBorrow(book.getBookId());
        borrowedBooks.add(book);
    }

    @SendMessage(from = "Library", to = "User")
    public void getOrderedBook(Book book) {
        // todo: this is for development, remove it when submitting
        // assert canBorrow(book.getBookId());
        borrowedBooks.add(book);
        orderedBooks.remove(book.getBookId());
    }

    public boolean hasOrdered(LibraryBookId bookId) {
        return orderedBooks.contains(bookId);
    }

    @SendMessage(from = "Library", to = "User")
    public void orderNewBook(LibraryBookId bookId) {
        assert canOrder(bookId);
        orderedBooks.add(bookId);
    }

    public boolean canOrder(LibraryBookId bookId) {
        if (creditScore < 0) {
            return false;
        }
        if (bookId.isTypeA() || bookId.isTypeAU() || bookId.isTypeBU() || bookId.isTypeCU()) {
            return false;
        } else if (bookId.isTypeB()) {
            return borrowedBooks.stream().noneMatch(book -> book.getBookId().isTypeB())
                    && orderedBooks.stream().noneMatch(LibraryBookId::isTypeB);
        } else {
            return borrowedBooks.stream().noneMatch(book -> book.getBookId().equals(bookId))
                    && !orderedBooks.contains(bookId);
        }
    }

    public boolean canBorrow(LibraryBookId bookId) {
        if (bookId.isTypeA() || bookId.isTypeAU()) {
            return false;
        } else if (bookId.isTypeB()) {
            return borrowedBooks.stream().noneMatch(book -> book.getBookId().isTypeB());
        } else if (bookId.isTypeBU()) {
            return borrowedBooks.stream().noneMatch(book -> book.getBookId().isTypeBU());
        } else {
            return borrowedBooks.stream().noneMatch(book -> book.getBookId().equals(bookId));
        }
    }

    public Book returnBook(LibraryBookId bookId) {
        Book res = null;
        Iterator<Book> iterator = borrowedBooks.iterator();
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
        return id + ": " + borrowedBooks;
    }

    public boolean try2RenewBook(LibraryBookId bookId, LocalDate today) {
        if (bookId.isTypeAU() || bookId.isTypeBU() || bookId.isTypeCU()) {
            return false;
        }
        Book book = borrowedBooks.stream().filter(b -> b.getBookId().equals(bookId))
                        .findFirst().orElse(null);
        assert book != null;
        return book.try2Renew(today);
    }

    public void removeOrder(LibraryBookId bookId) {
        assert hasOrdered(bookId);
        orderedBooks.remove(bookId);
    }

    public int getCreditScore() {
        return creditScore;
    }

    public void checkReturnDeadline(LocalDate today) {
        borrowedBooks.forEach(book -> {
            if (book.getTag() && book.getReturnDeadline().isBefore(today)) {
                creditScore -= 2;
                book.setTag(false);
            }
        });
    }

    public void addCreditScore(int num) {
        creditScore = Math.min(MAX_CREDIT_SCORE, creditScore + num);
    }
}
