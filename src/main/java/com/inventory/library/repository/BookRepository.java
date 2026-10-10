package com.inventory.library.repository;

import com.inventory.library.model.Book;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface defining persistence contracts for Book entities.
 * Enhanced in Week 4 to support sorting and paginated queries for optimized
 * memory and retrieval performance.
 */
public interface BookRepository {

    /**
     * Persists a book in the repository.
     */
    Book save(Book book);

    /**
     * Locates a book by its unique ISBN.
     */
    Optional<Book> findByIsbn(String isbn);

    /**
     * Retrieves all books currently stored.
     */
    List<Book> findAll();

    /**
     * Retrieves all books sorted by a given Comparator.
     */
    List<Book> findAll(Comparator<Book> comparator);

    /**
     * Retrieves a paginated subset of books, optionally sorted.
     *
     * @param pageNumber 1-based page index
     * @param pageSize   Number of items per page
     * @param comparator Sorting comparator (or null for default insertion order)
     * @return Paginated list of books
     */
    List<Book> findPaged(int pageNumber, int pageSize, Comparator<Book> comparator);

    /**
     * Fast search for books matching title query.
     */
    List<Book> findByTitle(String titleQuery);

    /**
     * Fast search for books matching author query.
     */
    List<Book> findByAuthor(String authorQuery);

    /**
     * Checks if a book exists by ISBN.
     */
    boolean existsByIsbn(String isbn);

    /**
     * Updates an existing book.
     */
    boolean update(Book book);

    /**
     * Deletes a book by ISBN.
     */
    boolean deleteByIsbn(String isbn);

    /**
     * Total count of books.
     */
    int count();

    /**
     * Clears all stored books.
     */
    void clear();
}
