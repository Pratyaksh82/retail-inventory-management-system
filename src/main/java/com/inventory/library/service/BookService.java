package com.inventory.library.service;

import com.inventory.library.exception.BookNotFoundException;
import com.inventory.library.exception.DuplicateBookException;
import com.inventory.library.exception.InvalidBookDataException;
import com.inventory.library.model.Book;
import com.inventory.library.repository.BookRepository;

import java.time.Year;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Service layer orchestrating business rules, input validations, and CRUD operations
 * for the Library Book Inventory application.
 */
public class BookService {

    private final BookRepository repository;

    // Pattern for alphanumeric ISBN-10 or ISBN-13 (ignoring hyphens and spaces)
    private static final Pattern ISBN_CLEAN_PATTERN = Pattern.compile("^[0-9]{9}[0-9X]$|^[0-9]{13}$");

    /**
     * Dependency injection constructor.
     *
     * @param repository The book repository implementation to use
     */
    public BookService(BookRepository repository) {
        this.repository = Objects.requireNonNull(repository, "BookRepository must not be null");
    }

    /**
     * Adds a new book to the inventory after performing thorough validation.
     *
     * @param book The book entity to add
     * @return The persisted book entity
     * @throws InvalidBookDataException if validation fails
     * @throws DuplicateBookException   if a book with the same ISBN already exists
     */
    public Book addBook(Book book) {
        validateBook(book);

        if (repository.existsByIsbn(book.getIsbn())) {
            throw new DuplicateBookException("A book with ISBN '" + book.getIsbn() + "' already exists in inventory.");
        }

        return repository.save(book);
    }

    /**
     * Retrieves a book by its ISBN.
     *
     * @param isbn Target ISBN
     * @return The Book entity
     * @throws BookNotFoundException if no matching book is found
     */
    public Book getBookByIsbn(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            throw new InvalidBookDataException("ISBN cannot be null or empty.");
        }

        return repository.findByIsbn(isbn)
                .orElseThrow(() -> new BookNotFoundException("Book with ISBN '" + isbn + "' was not found."));
    }

    /**
     * Returns all books currently in inventory.
     *
     * @return List of all books
     */
    public List<Book> getAllBooks() {
        return repository.findAll();
    }

    /**
     * Searches for books matching title or author query.
     *
     * @param query Search query
     * @return List of matching books without duplicates
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
     *
     * @param isbn            ISBN of the book to update
     * @param newTitle        Updated title
     * @param newAuthor       Updated author
     * @param newPubYear      Updated publication year
     * @return The updated Book entity
     * @throws BookNotFoundException   if book is not found
     * @throws InvalidBookDataException if input values are invalid
     */
    public Book updateBook(String isbn, String newTitle, String newAuthor, int newPubYear) {
        Book existing = getBookByIsbn(isbn);

        validateTitle(newTitle);
        validateAuthor(newAuthor);
        validatePublicationYear(newPubYear);

        existing.setTitle(newTitle.trim());
        existing.setAuthor(newAuthor.trim());
        existing.setPublicationYear(newPubYear);

        boolean updated = repository.update(existing);
        if (!updated) {
            throw new BookNotFoundException("Failed to update: Book with ISBN '" + isbn + "' no longer exists.");
        }
        return existing;
    }

    /**
     * Deletes a book by ISBN.
     *
     * @param isbn ISBN of the book to remove
     * @return The deleted Book entity
     * @throws BookNotFoundException if book is not found
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
     * Returns total book count.
     */
    public int getTotalCount() {
        return repository.count();
    }

    /**
     * Validates complete Book entity.
     */
    public void validateBook(Book book) {
        if (book == null) {
            throw new InvalidBookDataException("Book cannot be null.");
        }
        validateIsbn(book.getIsbn());
        validateTitle(book.getTitle());
        validateAuthor(book.getAuthor());
        validatePublicationYear(book.getPublicationYear());
    }

    /**
     * Validates ISBN syntax (accepts ISBN-10 or ISBN-13 with/without hyphens).
     */
    public void validateIsbn(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            throw new InvalidBookDataException("ISBN is required and cannot be blank.");
        }
        String clean = isbn.replaceAll("[\\s-]", "").toUpperCase();
        if (!ISBN_CLEAN_PATTERN.matcher(clean).matches()) {
            throw new InvalidBookDataException("Invalid ISBN format '" + isbn + "'. Must be valid 10-digit or 13-digit ISBN.");
        }
    }

    /**
     * Validates Title.
     */
    public void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new InvalidBookDataException("Book title is required and cannot be blank.");
        }
        if (title.trim().length() > 250) {
            throw new InvalidBookDataException("Book title is too long (maximum 250 characters).");
        }
    }

    /**
     * Validates Author.
     */
    public void validateAuthor(String author) {
        if (author == null || author.trim().isEmpty()) {
            throw new InvalidBookDataException("Author name is required and cannot be blank.");
        }
        if (author.trim().length() > 150) {
            throw new InvalidBookDataException("Author name is too long (maximum 150 characters).");
        }
    }

    /**
     * Validates Publication Year.
     */
    public void validatePublicationYear(int year) {
        int maxAllowedYear = Year.now().getValue() + 1; // allow books announced for upcoming year
        if (year < 1400 || year > maxAllowedYear) {
            throw new InvalidBookDataException("Publication year must be between 1400 and " + maxAllowedYear + " (got: " + year + ").");
        }
    }
}
