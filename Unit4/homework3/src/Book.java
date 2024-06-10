import com.oocourse.library3.LibraryBookId;
import com.oocourse.library3.annotation.Trigger;

import java.time.LocalDate;

public class Book {
    private LibraryBookId bookId;
    private State state;
    private LocalDate pickDeadline;
    private User owner;
    private final User donor;           // for donated book
    private int returnCnt = 0;
    private LocalDate returnDeadline;
    private boolean tag = true;

    public enum State {
        FREE,       // free to be borrowed, reserved or moved to bookshelf
        RESERVED,   // reserved by user
        BORROWED    // borrowed by user
    }

    public Book(LibraryBookId bookId) {
        this.bookId = bookId;
        this.state = State.FREE;
        this.pickDeadline = null;
        this.owner = null;
        this.donor = null;
    }
    
    public Book(LibraryBookId bookId, User donor) {
        this.bookId = bookId;
        this.state = State.FREE;
        this.pickDeadline = null;
        this.owner = null;
        this.donor = donor;
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

    @Trigger(from = "FREE", to = "RESERVED")
    public void setReserved(User user) {
        assert state == State.FREE;
        state = State.RESERVED;
        owner = user;
    }

    public void setPickDeadline(LocalDate currentDate) {
        pickDeadline = currentDate.plusDays(4);
    }

    @Trigger(from = "FREE", to = "BORROWED")
    @Trigger(from = "RESERVED", to = "BORROWED")
    public void setBorrowed(User user, LocalDate today) {
        assert state != State.BORROWED;
        state = State.BORROWED;
        owner = user;
        tag = true;
        switch (bookId.getType()) {
            case B:
                returnDeadline = today.plusDays(30);
                break;
            case BU:
                returnDeadline = today.plusDays(7);
                break;
            case C:
                returnDeadline = today.plusDays(60);
                break;
            case CU:
                returnDeadline = today.plusDays(14);
                break;
            default:
                throw new IllegalArgumentException("Cannot borrow A or AU book");
        }
    }

    @Trigger(from = "BORROWED", to = "FREE")
    public void setReturned() {
        assert state == State.BORROWED;
        state = State.FREE;
        owner = null;
        if (!isFormal()) {
            returnCnt++;
        }
    }

    public void putBackToBookShelf() {
        assert state == State.FREE;
        owner = null;
    }

    public boolean canMoveToBookShelf() {
        return state == State.FREE;
    }

    @Trigger(from = "RESERVED", to = {"FREE", "RESERVED"})
    public void updateState(LocalDate date) {
        if (date.isAfter(pickDeadline)) {
            state = State.FREE;
            owner.addCreditScore(-3);
            pickDeadline = null;
            owner.removeOrder(bookId);
            owner = null;
        }
    }

    public boolean canBePicked(User user, LocalDate date) {
        return state == State.RESERVED && owner.equals(user) && !date.isAfter(pickDeadline)
                        && user.canBorrow(bookId);
    }

    /**
     * Check if the book can be upgraded to an official library edition.
     * If so, update the {@code libraryBookId} and give donor 2 credit scores.
     * @return true if the book is set to formal book
     */
    public boolean try2SetFormal() {
        if (canBeSetFormal()) {
            donor.addCreditScore(2);
            bookId = new LibraryBookId(convert(bookId.getType()), bookId.getUid());
            return true;
        } else {
            return false;
        }
    }

    public boolean canBeSetFormal() {
        return !isFormal() && returnCnt >= 2;
    }

    public boolean isFormal() {
        return bookId.isTypeA() || bookId.isTypeB() || bookId.isTypeC();
    }

    private static LibraryBookId.Type convert(LibraryBookId.Type type) {
        switch (type) {
            case AU:
                return LibraryBookId.Type.A;
            case BU:
                return LibraryBookId.Type.B;
            case CU:
                return LibraryBookId.Type.C;
            default:
                throw new IllegalArgumentException("Cannot convert a format BookId");
        }
    }

    public boolean try2Renew(LocalDate today) {
        // ddl - 5 < today <= ddl
        if (today.isAfter(returnDeadline.minusDays(5)) && !today.isAfter(returnDeadline)) {
            returnDeadline = returnDeadline.plusDays(30);
            return true;
        } else {
            return false;
        }
    }

    public LocalDate getReturnDeadline() {
        return returnDeadline;
    }

    @Override
    public String toString() {
        return bookId.toString() + "(" + state + ")";
    }

    public boolean getTag() {
        return tag;
    }

    public void setTag(boolean tag) {
        this.tag = tag;
    }
}
