package com.inventory.library.service;

import com.inventory.library.exception.BookNotFoundException;
import com.inventory.library.exception.DuplicateBookException;
import com.inventory.library.exception.InvalidBookDataException;
import com.inventory.library.model.Book;
import com.inventory.library.repository.BookRepository;
import com.inventory.library.util.BookValidator;

import java.util.*;

/**
 * Service layer orchestrating business operations, validation rules, and lifecycle logic
 * for the Library Book Inventory application.
 *
 * Refactored in Week 4:
 * - Delegates validation to DRY {@link BookValidator}.
 * - Employs {@link Book.Builder} for safe mutation.
 * - Added sorting and paginated retrieval capabilities.
 */
public class BookService {

    private final BookRepository repository;

    /**
     * Dependency injection constructor.
     *
     * @param repository The book repository implementation
     */
    public BookService(BookRepository repository) {
        this.repository = Objects.requireNonNull(repository, "BookRepository must not be null");
    }

    /**
     * Adds a new book after executing domain validations and duplicate checks.
     */
    public Book addBook(Book book) {
        BookValidator.validateBook(book);

        if (repository.existsByIsbn(book.getIsbn())) {
            throw new DuplicateBookException("A book with ISBN '" + book.getIsbn() + "' already exists in inventory.");
        }

        return repository.save(book);
    }

    /**
     * Retrieves a book by ISBN.
     */
    public Book getBookByIsbn(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            throw new InvalidBookDataException("ISBN cannot be null or empty.");
        }

        return repository.findByIsbn(isbn)
                .orElseThrow(() -> new BookNotFoundException("Book with ISBN '" + isbn + "' was not found."));
    }

    /**
     * Retrieves all books in default insertion order.
     */
    public List<Book> getAllBooks() {
        return repository.findAll();
    }

    /**
     * Retrieves all books sorted by a custom comparator.
     */
    public List<Book> getAllBooksSorted(Comparator<Book> comparator) {
        return repository.findAll(comparator);
    }

    /**
     * Retrieves paginated results.
     */
    public List<Book> getBooksPaged(int pageNumber, int pageSize, Comparator<Book> comparator) {
        return repository.findPaged(pageNumber, pageSize, comparator);
    }

    /**
     * Searches for books matching title or author query.
     */
    public List<Book> searchBooks(String query) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }

        Set<Book> results = new LinkedHashSet<>();
        results.addAll(repository.findByTitle(query));
        results.addAll(repository.findByAuthor(query));
        return new ArrayList<>(results);
    }

    /**
     * Updates an existing book's title, author, and publication year.
     */
    public Book updateBook(String isbn, String newTitle, String newAuthor, int newPubYear) {
        Book existing = getBookByIsbn(isbn);

        BookValidator.validateTitle(newTitle);
        BookValidator.validateAuthor(newAuthor);
        BookValidator.validatePublicationYear(newPubYear);

        Book updated = existing.toBuilder()
                .title(newTitle.trim())
                .author(newAuthor.trim())
                .publicationYear(newPubYear)
                .build();

        boolean success = repository.update(updated);
        if (!success) {
            throw new BookNotFoundException("Failed to update: Book with ISBN '" + isbn + "' no longer exists.");
        }
        return updated;
    }

    /**
     * Deletes a book by ISBN.
     */
    public Book deleteBook(String isbn) {
        Book existing = getBookByIsbn(isbn);
        boolean removed = repository.deleteByIsbn(isbn);
        if (!removed) {
            throw new BookNotFoundException("Could not remove book with ISBN '" + isbn + "'.");
        }
        return existing;
    }

    /**
     * Total count of books.
     */
    public int getTotalCount() {
        return repository.count();
    }

    // --- Backward-Compatible Validation Delegates (DRY) ---

    public void validateBook(Book book) {
        BookValidator.validateBook(book);
    }

    public void validateIsbn(String isbn) {
        BookValidator.validateIsbn(isbn);
    }

    public void validateTitle(String title) {
        BookValidator.validateTitle(title);
    }

    public void validateAuthor(String author) {
        BookValidator.validateAuthor(author);
    }

    public void validatePublicationYear(int year) {
        BookValidator.validatePublicationYear(year);
    }
}
