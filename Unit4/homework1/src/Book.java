import com.oocourse.library1.LibraryBookId;

import java.time.LocalDate;

public class Book {
    private final LibraryBookId bookId;
    private final int copyId;
    private State state;
    private LocalDate latestPickDate;
    private User owner;

    public enum State {
        FREE,       // free to be borrowed, reserved or moved to bookshelf
        RESERVED,   // reserved by user
        BORROWED    // borrowed by user
    }

    public Book(LibraryBookId bookId, int copyId) {
        this.bookId = bookId;
        this.copyId = copyId;
        this.state = State.FREE;
        this.latestPickDate = null;
        this.owner = null;
    }

    public LibraryBookId getBookId() {
        return bookId;
    }

    public User getOwner() {
        return owner;
    }

    public State getState() {
        return state;
    }

    public void setReserved(User user) {
        assert state == State.FREE;
        state = State.RESERVED;
        owner = user;
    }

    public void setLatestPickDate(LocalDate currentDate) {
        latestPickDate = currentDate.plusDays(4);
    }

    public void setBorrowed(User user) {
        assert state == State.RESERVED;
        state = State.BORROWED;
        owner = user;
    }

    public void setReturned() {
        assert state == State.BORROWED;
        state = State.FREE;
        owner = null;
    }

    public void putBackToBookShelf() {
        assert state == State.FREE;
        owner = null;
    }

    public boolean canMoveToBookShelf() {
        return state == State.FREE;
    }

    public void updateState(LocalDate date) {
        if (date.isAfter(latestPickDate)) {
            state = State.FREE;
            latestPickDate = null;
            owner = null;
        }
    }

    public boolean canBePicked(User user, LocalDate date) {
        return state == State.RESERVED && owner.equals(user) && !date.isAfter(latestPickDate)
                        && user.canBorrowOrOrder(bookId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Book) {
            Book book = (Book) obj;
            return book.getBookId().equals(bookId) && book.copyId == copyId;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return bookId.toString() + "-" + copyId + "(" + state + ")";
    }
}
