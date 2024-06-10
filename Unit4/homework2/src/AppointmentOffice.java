import com.oocourse.library2.LibraryBookId;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class AppointmentOffice extends BookManager {
    public AppointmentOffice() {
        super();
    }

    public List<Book> removeBooksForBookshelf(LocalDate date) {
        List<Book> result = new ArrayList<>();
        getBooks().forEach(((bookId, books) -> {
            books.forEach(book -> book.updateState(date));
        }));
        for (ArrayList<Book> books : getBooks().values()) {
            result.addAll(books.stream()
                    .filter(Book::canMoveToBookShelf)
                    .collect(Collectors.toList()));
            books.removeIf(Book::canMoveToBookShelf);
        }
        return result;
    }

    public Book popBookForPick(LibraryBookId bookId, User user, LocalDate date) {
        if (!hasBook(bookId)) {
            return null;
        }
        ArrayList<Book> books = getBooks(bookId);
        Iterator<Book> iterator = books.iterator();
        while (iterator.hasNext()) {
            Book book = iterator.next();
            if (book.canBePicked(user, date)) {
                book.setBorrowed(user, date);
                user.pickBook(book);
                iterator.remove();
                return book;
            }
        }
        return null;
    }
}
