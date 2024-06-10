import com.oocourse.library3.LibraryBookId;

public class BookDriftCorner extends BookManager {
    public Book popBookForBorrow(LibraryBookId bookId) {
        if (bookId.isTypeAU()) {
            return null;
        }
        Book book = getBook(bookId);
        if (book != null) {
            removeBook(book);
        }
        return book;
    }
}
