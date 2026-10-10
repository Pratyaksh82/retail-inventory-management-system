package com.inventory.library.repository;

import com.inventory.library.model.Book;

import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory implementation of {@link BookRepository} utilizing Java Collections framework.
 * Uses a {@link LinkedHashMap} to maintain insertion order while offering O(1) time
 * complexity for ISBN-based retrieval, existence check, update, and deletion operations.
 */
public class InMemoryBookRepository implements BookRepository {

    /**
     * In-memory storage mapping normalized ISBN to Book instance.
     * LinkedHashMap guarantees predictable iteration ordering.
     */
    private final Map<String, Book> storage = new LinkedHashMap<>();

    /**
     * Normalizes an ISBN by stripping spaces and hyphens, and converting to uppercase.
     */
    private String normalizeIsbn(String isbn) {
        if (isbn == null) {
            return "";
        }
        return isbn.replaceAll("[\\s-]", "").toUpperCase();
    }

    @Override
    public synchronized Book save(Book book) {
        if (book == null || book.getIsbn() == null) {
            throw new IllegalArgumentException("Book and Book ISBN must not be null");
        }
        String key = normalizeIsbn(book.getIsbn());
        // Store a defensive copy or preserve exact reference
        storage.put(key, book);
        return book;
    }

    @Override
    public synchronized Optional<Book> findByIsbn(String isbn) {
        if (isbn == null) {
            return Optional.empty();
        }
        String key = normalizeIsbn(isbn);
        return Optional.ofNullable(storage.get(key));
    }

    @Override
    public synchronized List<Book> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public synchronized List<Book> findByTitle(String titleQuery) {
        if (titleQuery == null || titleQuery.isBlank()) {
            return Collections.emptyList();
        }
        String q = titleQuery.trim().toLowerCase();
        return storage.values().stream()
                .filter(b -> b.getTitle() != null && b.getTitle().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    @Override
    public synchronized List<Book> findByAuthor(String authorQuery) {
        if (authorQuery == null || authorQuery.isBlank()) {
            return Collections.emptyList();
        }
        String q = authorQuery.trim().toLowerCase();
        return storage.values().stream()
                .filter(b -> b.getAuthor() != null && b.getAuthor().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    @Override
    public synchronized boolean existsByIsbn(String isbn) {
        if (isbn == null) {
            return false;
        }
        return storage.containsKey(normalizeIsbn(isbn));
    }

    @Override
    public synchronized boolean update(Book book) {
        if (book == null || book.getIsbn() == null) {
            return false;
        }
        String key = normalizeIsbn(book.getIsbn());
        if (storage.containsKey(key)) {
            storage.put(key, book);
            return true;
        }
        return false;
    }

    @Override
    public synchronized boolean deleteByIsbn(String isbn) {
        if (isbn == null) {
            return false;
        }
        String key = normalizeIsbn(isbn);
        return storage.remove(key) != null;
    }

    @Override
    public synchronized int count() {
        return storage.size();
    }

    @Override
    public synchronized void clear() {
        storage.clear();
    }
}
