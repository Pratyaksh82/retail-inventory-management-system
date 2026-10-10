package com.inventory.library;

import com.inventory.library.exception.BookNotFoundException;
import com.inventory.library.exception.DuplicateBookException;
import com.inventory.library.exception.InvalidBookDataException;
import com.inventory.library.model.Book;
import com.inventory.library.repository.InMemoryBookRepository;
import com.inventory.library.service.BookService;

import java.util.List;

/**
 * Standalone test runner that validates all CRUD operations, input validations,
 * collection integrity, and custom exceptions without requiring external test runners.
 */
public class LibraryTestRunner {

    private int testsPassed = 0;
    private int testsFailed = 0;

    public static void main(String[] args) {
        LibraryTestRunner runner = new LibraryTestRunner();
        runner.runAllTests();
    }

    public void runAllTests() {
        System.out.println("==========================================================");
        System.out.println(" RUNNING AUTOMATED UNIT TESTS FOR LIBRARY INVENTORY SYSTEM");
        System.out.println("==========================================================");

        testCreateBookSuccess();
        testDuplicateIsbnThrowsException();
        testInvalidBookDataThrowsException();
        testFindBookByIsbnSuccess();
        testFindNonExistentBookThrowsException();
        testListAllBooksOrderPreserved();
        testSearchBooksByTitleAndAuthor();
        testUpdateBookSuccess();
        testDeleteBookSuccess();
        testDeleteNonExistentBookThrowsException();

        System.out.println("----------------------------------------------------------");
        System.out.printf("Test Results: %d Passed | %d Failed%n", testsPassed, testsFailed);
        System.out.println("==========================================================");

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private void assertTrue(String testName, boolean condition) {
        if (condition) {
            System.out.println("  PASS: " + testName);
            testsPassed++;
        } else {
            System.err.println("  FAIL: " + testName);
            testsFailed++;
        }
    }

    private void assertEquals(String testName, Object expected, Object actual) {
        if (expected == null && actual == null || (expected != null && expected.equals(actual))) {
            System.out.println("  PASS: " + testName);
            testsPassed++;
        } else {
            System.err.println("  FAIL: " + testName + " - Expected: " + expected + ", Actual: " + actual);
            testsFailed++;
        }
    }

    private void testCreateBookSuccess() {
        BookService service = new BookService(new InMemoryBookRepository());
        Book book = new Book("978-0134685991", "Effective Java", "Joshua Bloch", 2018);
        Book saved = service.addBook(book);

        assertEquals("Create Book - ISBN matches", "978-0134685991", saved.getIsbn());
        assertEquals("Create Book - Total count is 1", 1, service.getTotalCount());
    }

    private void testDuplicateIsbnThrowsException() {
        BookService service = new BookService(new InMemoryBookRepository());
        service.addBook(new Book("978-0134685991", "Effective Java", "Joshua Bloch", 2018));

        boolean threwDuplicate = false;
        try {
            service.addBook(new Book("978-0134685991", "Duplicate Entry", "Another Author", 2020));
        } catch (DuplicateBookException e) {
            threwDuplicate = true;
        }
        assertTrue("Duplicate ISBN throws DuplicateBookException", threwDuplicate);
    }

    private void testInvalidBookDataThrowsException() {
        BookService service = new BookService(new InMemoryBookRepository());

        // Blank Title
        boolean blankTitleCaught = false;
        try {
            service.addBook(new Book("978-0134685991", "   ", "Joshua Bloch", 2018));
        } catch (InvalidBookDataException e) {
            blankTitleCaught = true;
        }
        assertTrue("Validation - Blank title rejected", blankTitleCaught);

        // Invalid Year (future out of range)
        boolean invalidYearCaught = false;
        try {
            service.addBook(new Book("978-0134685991", "Future Book", "Author", 3000));
        } catch (InvalidBookDataException e) {
            invalidYearCaught = true;
        }
        assertTrue("Validation - Future year 3000 rejected", invalidYearCaught);

        // Malformed ISBN
        boolean invalidIsbnCaught = false;
        try {
            service.addBook(new Book("invalid-isbn-123", "Title", "Author", 2020));
        } catch (InvalidBookDataException e) {
            invalidIsbnCaught = true;
        }
        assertTrue("Validation - Malformed ISBN rejected", invalidIsbnCaught);
    }

    private void testFindBookByIsbnSuccess() {
        BookService service = new BookService(new InMemoryBookRepository());
        service.addBook(new Book("978-0132350884", "Clean Code", "Robert C. Martin", 2008));

        Book found = service.getBookByIsbn("978-0132350884");
        assertEquals("Find Book by ISBN - Title matches", "Clean Code", found.getTitle());
    }

    private void testFindNonExistentBookThrowsException() {
        BookService service = new BookService(new InMemoryBookRepository());

        boolean notFoundCaught = false;
        try {
            service.getBookByIsbn("978-0000000000");
        } catch (BookNotFoundException e) {
            notFoundCaught = true;
        }
        assertTrue("Find non-existent ISBN throws BookNotFoundException", notFoundCaught);
    }

    private void testListAllBooksOrderPreserved() {
        BookService service = new BookService(new InMemoryBookRepository());
        service.addBook(new Book("978-0134685991", "Book 1", "Author A", 2010));
        service.addBook(new Book("978-0596009205", "Book 2", "Author B", 2015));
        service.addBook(new Book("978-0132350884", "Book 3", "Author C", 2020));

        List<Book> books = service.getAllBooks();
        assertEquals("List all books - Size is 3", 3, books.size());
        assertEquals("List all books - First is Book 1", "Book 1", books.get(0).getTitle());
        assertEquals("List all books - Last is Book 3", "Book 3", books.get(2).getTitle());
    }

    private void testSearchBooksByTitleAndAuthor() {
        BookService service = new BookService(new InMemoryBookRepository());
        service.addBook(new Book("978-0134685991", "Effective Java", "Joshua Bloch", 2018));
        service.addBook(new Book("978-0596009205", "Head First Java", "Kathy Sierra", 2022));
        service.addBook(new Book("978-0132350884", "Clean Code", "Robert C. Martin", 2008));

        List<Book> javaMatches = service.searchBooks("java");
        assertEquals("Search 'java' returns 2 books", 2, javaMatches.size());

        List<Book> authorMatches = service.searchBooks("Martin");
        assertEquals("Search 'Martin' returns 1 book", 1, authorMatches.size());
    }

    private void testUpdateBookSuccess() {
        BookService service = new BookService(new InMemoryBookRepository());
        service.addBook(new Book("978-0134685991", "Java Old Title", "Old Author", 2010));

        Book updated = service.updateBook("978-0134685991", "Effective Java (3rd Edition)", "Joshua Bloch", 2018);
        assertEquals("Update Book - Title updated", "Effective Java (3rd Edition)", updated.getTitle());
        assertEquals("Update Book - Author updated", "Joshua Bloch", updated.getAuthor());
        assertEquals("Update Book - Year updated", 2018, updated.getPublicationYear());
    }

    private void testDeleteBookSuccess() {
        BookService service = new BookService(new InMemoryBookRepository());
        service.addBook(new Book("978-0134685991", "Effective Java", "Joshua Bloch", 2018));
        assertEquals("Count before delete", 1, service.getTotalCount());

        Book deleted = service.deleteBook("978-0134685991");
        assertEquals("Deleted book ISBN", "978-0134685991", deleted.getIsbn());
        assertEquals("Count after delete", 0, service.getTotalCount());
    }

    private void testDeleteNonExistentBookThrowsException() {
        BookService service = new BookService(new InMemoryBookRepository());

        boolean deleteMissingCaught = false;
        try {
            service.deleteBook("978-0000000000");
        } catch (BookNotFoundException e) {
            deleteMissingCaught = true;
        }
        assertTrue("Delete non-existent ISBN throws BookNotFoundException", deleteMissingCaught);
    }
}
