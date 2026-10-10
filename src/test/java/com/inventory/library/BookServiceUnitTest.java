package com.inventory.library;

import com.inventory.library.exception.BookNotFoundException;
import com.inventory.library.exception.DuplicateBookException;
import com.inventory.library.exception.InvalidBookDataException;
import com.inventory.library.model.Book;
import com.inventory.library.repository.InMemoryBookRepository;
import com.inventory.library.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Year;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive JUnit 5 unit test suite for the Library Book Inventory application.
 * Covers full CRUD functionality, input validations, boundary values, exception handling,
 * and regression verification for identified and resolved bugs.
 */
@DisplayName("BookService Unit Tests")
public class BookServiceUnitTest {

    private BookService bookService;
    private InMemoryBookRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryBookRepository();
        bookService = new BookService(repository);
    }

    @Nested
    @DisplayName("Create (Add Book) Tests")
    class CreateTests {

        @Test
        @DisplayName("Successfully add a valid book with standard ISBN-13")
        void testAddValidBookSuccess() {
            Book book = new Book("978-0134685991", "Effective Java", "Joshua Bloch", 2018);
            Book saved = bookService.addBook(book);

            assertNotNull(saved, "Saved book should not be null");
            assertEquals("978-0134685991", saved.getIsbn());
            assertEquals(1, bookService.getTotalCount(), "Inventory count should increment to 1");
        }

        @Test
        @DisplayName("Successfully add book with ISBN-10 ending in 'X'")
        void testAddValidBookIsbn10WithXSuccess() {
            Book book = new Book("0-306-40615-X", "Relativity Physics", "Albert Einstein", 1920);
            Book saved = bookService.addBook(book);

            assertNotNull(saved);
            assertEquals("0-306-40615-X", saved.getIsbn());
        }

        @Test
        @DisplayName("Boundary: Successfully add book at earliest valid publication year (1400)")
        void testAddBookEarliestAllowedPublicationYear() {
            Book book = new Book("978-0000000001", "Gutenberg Movable Type Bible", "Johannes Gutenberg", 1450);
            Book saved = bookService.addBook(book);
            assertEquals(1450, saved.getPublicationYear());
        }

        @Test
        @DisplayName("Boundary: Successfully add book published in upcoming year (Current + 1)")
        void testAddBookUpcomingPublicationYear() {
            int upcomingYear = Year.now().getValue() + 1;
            Book book = new Book("978-0000000002", "Next Generation Computing", "Tech Author", upcomingYear);
            Book saved = bookService.addBook(book);
            assertEquals(upcomingYear, saved.getPublicationYear());
        }

        @Test
        @DisplayName("Throw DuplicateBookException when adding identical ISBN")
        void testDuplicateIsbnThrowsException() {
            Book book1 = new Book("978-0134685991", "Effective Java", "Joshua Bloch", 2018);
            bookService.addBook(book1);

            Book book2 = new Book("978-0134685991", "Duplicate Entry", "Another Author", 2021);
            assertThrows(DuplicateBookException.class, () -> bookService.addBook(book2),
                    "Adding duplicate ISBN must throw DuplicateBookException");
        }

        @Test
        @DisplayName("Regression Bug Fix: Normalize ISBN hyphens to detect duplicate")
        void testDuplicateIsbnNormalizedHyphensThrowsException() {
            // First book added with hyphens
            bookService.addBook(new Book("978-0134685991", "Effective Java", "Joshua Bloch", 2018));

            // Second book added WITHOUT hyphens (same standard number)
            Book unhyphenated = new Book("9780134685991", "Effective Java (Reprint)", "Joshua Bloch", 2018);
            assertThrows(DuplicateBookException.class, () -> bookService.addBook(unhyphenated),
                    "Repository must normalize hyphens and detect duplicate");
        }
    }

    @Nested
    @DisplayName("Read (Query & Search) Tests")
    class ReadTests {

        @BeforeEach
        void seedBooks() {
            bookService.addBook(new Book("978-0134685991", "Effective Java", "Joshua Bloch", 2018));
            bookService.addBook(new Book("978-0596009205", "Head First Java", "Kathy Sierra", 2022));
            bookService.addBook(new Book("978-0132350884", "Clean Code", "Robert C. Martin", 2008));
        }

        @Test
        @DisplayName("Retrieve book by exact ISBN")
        void testGetBookByIsbnSuccess() {
            Book found = bookService.getBookByIsbn("978-0132350884");
            assertNotNull(found);
            assertEquals("Clean Code", found.getTitle());
            assertEquals("Robert C. Martin", found.getAuthor());
        }

        @Test
        @DisplayName("Retrieve book by unhyphenated ISBN")
        void testGetBookByUnhyphenatedIsbnSuccess() {
            Book found = bookService.getBookByIsbn("9780132350884");
            assertNotNull(found);
            assertEquals("Clean Code", found.getTitle());
        }

        @Test
        @DisplayName("Throw BookNotFoundException when ISBN does not exist")
        void testGetBookByMissingIsbnThrowsException() {
            assertThrows(BookNotFoundException.class, () -> bookService.getBookByIsbn("978-9999999999"));
        }

        @Test
        @DisplayName("List all books preserves insertion order (LinkedHashMap contract)")
        void testGetAllBooksPreservesInsertionOrder() {
            List<Book> all = bookService.getAllBooks();
            assertEquals(3, all.size());
            assertEquals("Effective Java", all.get(0).getTitle());
            assertEquals("Head First Java", all.get(1).getTitle());
            assertEquals("Clean Code", all.get(2).getTitle());
        }

        @Test
        @DisplayName("Search by title substring (case-insensitive)")
        void testSearchByTitleSubstring() {
            List<Book> results = bookService.searchBooks("java");
            assertEquals(2, results.size(), "Search for 'java' should match 2 books");
        }

        @Test
        @DisplayName("Search by author substring (case-insensitive)")
        void testSearchByAuthorSubstring() {
            List<Book> results = bookService.searchBooks("martin");
            assertEquals(1, results.size());
            assertEquals("Clean Code", results.get(0).getTitle());
        }

        @Test
        @DisplayName("Search with unmatched query returns empty list")
        void testSearchUnmatchedQueryReturnsEmpty() {
            List<Book> results = bookService.searchBooks("Quantum Physics");
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("Search with blank query returns empty list")
        void testSearchBlankQueryReturnsEmpty() {
            assertTrue(bookService.searchBooks("").isEmpty());
            assertTrue(bookService.searchBooks("   ").isEmpty());
            assertTrue(bookService.searchBooks(null).isEmpty());
        }
    }

    @Nested
    @DisplayName("Update Tests")
    class UpdateTests {

        @BeforeEach
        void seedBook() {
            bookService.addBook(new Book("978-0134685991", "Effective Java", "Joshua Bloch", 2018));
        }

        @Test
        @DisplayName("Update all fields of an existing book")
        void testUpdateAllFieldsSuccess() {
            Book updated = bookService.updateBook("978-0134685991", "Effective Java (3rd Edition)", "Joshua J. Bloch", 2019);

            assertEquals("Effective Java (3rd Edition)", updated.getTitle());
            assertEquals("Joshua J. Bloch", updated.getAuthor());
            assertEquals(2019, updated.getPublicationYear());

            // Verify in storage
            Book retrieved = bookService.getBookByIsbn("978-0134685991");
            assertEquals("Effective Java (3rd Edition)", retrieved.getTitle());
        }

        @Test
        @DisplayName("Throw BookNotFoundException when updating non-existent book")
        void testUpdateNonExistentThrowsException() {
            assertThrows(BookNotFoundException.class, () ->
                    bookService.updateBook("978-9999999999", "New Title", "New Author", 2020));
        }

        @Test
        @DisplayName("Throw InvalidBookDataException when updating with invalid publication year")
        void testUpdateInvalidYearThrowsException() {
            assertThrows(InvalidBookDataException.class, () ->
                    bookService.updateBook("978-0134685991", "Valid Title", "Valid Author", 3500));
        }
    }

    @Nested
    @DisplayName("Delete Tests")
    class DeleteTests {

        @BeforeEach
        void seedBook() {
            bookService.addBook(new Book("978-0134685991", "Effective Java", "Joshua Bloch", 2018));
        }

        @Test
        @DisplayName("Successfully delete an existing book and decrement count")
        void testDeleteBookSuccess() {
            assertEquals(1, bookService.getTotalCount());

            Book deleted = bookService.deleteBook("978-0134685991");
            assertEquals("Effective Java", deleted.getTitle());
            assertEquals(0, bookService.getTotalCount());

            // Ensure cannot find deleted book
            assertThrows(BookNotFoundException.class, () -> bookService.getBookByIsbn("978-0134685991"));
        }

        @Test
        @DisplayName("Throw BookNotFoundException when deleting non-existent book")
        void testDeleteNonExistentThrowsException() {
            assertThrows(BookNotFoundException.class, () -> bookService.deleteBook("978-9999999999"));
        }
    }

    @Nested
    @DisplayName("Validation and Boundary Tests")
    class ValidationTests {

        @Test
        @DisplayName("Reject null book")
        void testNullBookThrowsException() {
            assertThrows(InvalidBookDataException.class, () -> bookService.addBook(null));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t\n"})
        @DisplayName("Reject blank or whitespace titles")
        void testBlankTitleThrowsException(String blankTitle) {
            Book book = new Book("978-0134685991", blankTitle, "Joshua Bloch", 2018);
            assertThrows(InvalidBookDataException.class, () -> bookService.addBook(book));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   "})
        @DisplayName("Reject blank author names")
        void testBlankAuthorThrowsException(String blankAuthor) {
            Book book = new Book("978-0134685991", "Valid Title", blankAuthor, 2018);
            assertThrows(InvalidBookDataException.class, () -> bookService.addBook(book));
        }

        @ParameterizedTest
        @ValueSource(ints = {1200, 1399, -50, 3000, 2050})
        @DisplayName("Reject publication years out of allowed bounds [1400, Current+1]")
        void testOutOfRangeYearThrowsException(int invalidYear) {
            Book book = new Book("978-0134685991", "Valid Title", "Valid Author", invalidYear);
            assertThrows(InvalidBookDataException.class, () -> bookService.addBook(book));
        }

        @ParameterizedTest
        @ValueSource(strings = {"12345", "INVALID-ISBN", "978-013468599A", "12345678901234567"})
        @DisplayName("Reject malformed ISBN formats")
        void testMalformedIsbnThrowsException(String invalidIsbn) {
            Book book = new Book(invalidIsbn, "Valid Title", "Valid Author", 2020);
            assertThrows(InvalidBookDataException.class, () -> bookService.addBook(book));
        }
    }
}
