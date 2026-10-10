package com.inventory.library.repository;

import com.inventory.library.model.Book;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface defining CRUD and query persistence operations for Book entities.
 * Follows the Data Access Object / Repository design pattern to decouple business
 * services from storage implementations.
 */
public interface BookRepository {

    /**
     * Saves a new book into the repository.
     *
     * @param book Book to persist
     * @return The persisted Book
     */
    Book save(Book book);

    /**
     * Locates a book by its unique ISBN.
     *
     * @param isbn Book ISBN
     * @return Optional containing the book if found, empty otherwise
     */
    Optional<Book> findByIsbn(String isbn);

    /**
     * Retrieves all books currently stored in the repository.
     *
     * @return List of all books
     */
    List<Book> findAll();

    /**
     * Searches for books whose title contains the specified query (case-insensitive).
     *
     * @param titleQuery Search text
     * @return List of matching books
     */
    List<Book> findByTitle(String titleQuery);

    /**
     * Searches for books whose author contains the specified query (case-insensitive).
     *
     * @param authorQuery Search text
     * @return List of matching books
     */
    List<Book> findByAuthor(String authorQuery);

    /**
     * Checks if a book exists with the given ISBN.
     *
     * @param isbn Book ISBN
     * @return True if exists, false otherwise
     */
    boolean existsByIsbn(String isbn);

    /**
     * Updates the details of an existing book.
     *
     * @param book Book with updated attributes
     * @return True if updated successfully, false if not found
     */
    boolean update(Book book);

    /**
     * Deletes a book matching the given ISBN.
     *
     * @param isbn Book ISBN to delete
     * @return True if deleted, false if not found
     */
    boolean deleteByIsbn(String isbn);

    /**
     * Returns the total count of books in inventory.
     *
     * @return Count of books
     */
    int count();

    /**
     * Clears all stored books (primarily for test resets).
     */
    void clear();
}
