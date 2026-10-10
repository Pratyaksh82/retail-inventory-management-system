package com.inventory.library.util;

import com.inventory.library.exception.InvalidBookDataException;
import com.inventory.library.model.Book;

import java.time.Year;
import java.util.regex.Pattern;

/**
 * Centralized utility class enforcing DRY (Don't Repeat Yourself) validation logic
 * across all layers of the application.
 */
public final class BookValidator {

    private static final int MAX_TITLE_LENGTH = 250;
    private static final int MAX_AUTHOR_LENGTH = 150;
    private static final int MIN_PUBLICATION_YEAR = 1400;

    // Supports 10-digit (with optional 'X' check digit) or 13-digit standard ISBNs
    private static final Pattern ISBN_CLEAN_PATTERN = Pattern.compile("^[0-9]{9}[0-9X]$|^[0-9]{13}$");

    private BookValidator() {
        // Prevent instantiation of utility class
    }

    /**
     * Normalizes an ISBN string by stripping whitespace and hyphens and converting to uppercase.
     *
     * @param isbn Input ISBN
     * @return Canonical normalized ISBN
     */
    public static String normalizeIsbn(String isbn) {
        if (isbn == null) {
            return "";
        }
        return isbn.replaceAll("[\\s-]", "").toUpperCase();
    }

    /**
     * Validates a complete Book entity.
     *
     * @param book Target Book
     * @throws InvalidBookDataException if any rule fails
     */
    public static void validateBook(Book book) {
        if (book == null) {
            throw new InvalidBookDataException("Book cannot be null.");
        }
        validateIsbn(book.getIsbn());
        validateTitle(book.getTitle());
        validateAuthor(book.getAuthor());
        validatePublicationYear(book.getPublicationYear());
    }

    /**
     * Validates ISBN format and presence.
     */
    public static void validateIsbn(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            throw new InvalidBookDataException("ISBN is required and cannot be blank.");
        }
        String clean = normalizeIsbn(isbn);
        if (!ISBN_CLEAN_PATTERN.matcher(clean).matches()) {
            throw new InvalidBookDataException("Invalid ISBN format '" + isbn + "'. Must be valid 10-digit or 13-digit ISBN.");
        }
    }

    /**
     * Validates Book title presence and length bounds.
     */
    public static void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new InvalidBookDataException("Book title is required and cannot be blank.");
        }
        if (title.trim().length() > MAX_TITLE_LENGTH) {
            throw new InvalidBookDataException("Book title is too long (maximum " + MAX_TITLE_LENGTH + " characters).");
        }
    }

    /**
     * Validates Author presence and length bounds.
     */
    public static void validateAuthor(String author) {
        if (author == null || author.trim().isEmpty()) {
            throw new InvalidBookDataException("Author name is required and cannot be blank.");
        }
        if (author.trim().length() > MAX_AUTHOR_LENGTH) {
            throw new InvalidBookDataException("Author name is too long (maximum " + MAX_AUTHOR_LENGTH + " characters).");
        }
    }

    /**
     * Validates Publication Year within valid historical and modern publication bounds.
     */
    public static void validatePublicationYear(int year) {
        int maxAllowedYear = Year.now().getValue() + 1;
        if (year < MIN_PUBLICATION_YEAR || year > maxAllowedYear) {
            throw new InvalidBookDataException("Publication year must be between " + MIN_PUBLICATION_YEAR
                    + " and " + maxAllowedYear + " (got: " + year + ").");
        }
    }
}
