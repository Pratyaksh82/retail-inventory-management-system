package com.inventory.library.repository;

import com.inventory.library.model.Book;
import com.inventory.library.util.BookValidator;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * High-performance, thread-safe in-memory repository implementing {@link BookRepository}.
 *
 * Week 4 Refactoring and Optimization Highlights:
 * 1. Concurrency Optimization: Replaced coarse method-level synchronization with ReentrantReadWriteLock.
 *    Multiple readers can access data concurrently without lock contention, while writes acquire exclusive locks.
 * 2. Algorithmic Search Optimization: Implemented secondary inverted token indexing for titles and authors,
 *    reducing search overhead from brute-force O(n) scans to fast token-indexed lookups.
 * 3. Pagination & Sorting: Native support for paginated streaming queries with custom comparators.
 * 4. Defensive Encapsulation: Strict defensive copying prevents external mutation of stored collection records.
 */
public class InMemoryBookRepository implements BookRepository {

    // Primary storage preserving insertion order
    private final Map<String, Book> storage = new LinkedHashMap<>();

    // Secondary inverted index: maps lowercase word tokens -> set of matching normalized ISBNs
    private final Map<String, Set<String>> titleIndex = new HashMap<>();
    private final Map<String, Set<String>> authorIndex = new HashMap<>();

    // High-concurrency Read/Write Lock
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = rwLock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = rwLock.writeLock();

    @Override
    public Book save(Book book) {
        if (book == null || book.getIsbn() == null) {
            throw new IllegalArgumentException("Book and Book ISBN must not be null");
        }
        String key = BookValidator.normalizeIsbn(book.getIsbn());

        writeLock.lock();
        try {
            // Store defensive copy to avoid external mutation leaks
            Book copy = book.toBuilder().build();
            storage.put(key, copy);
            indexBook(key, copy);
            return copy;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Optional<Book> findByIsbn(String isbn) {
        if (isbn == null) {
            return Optional.empty();
        }
        String key = BookValidator.normalizeIsbn(isbn);

        readLock.lock();
        try {
            Book book = storage.get(key);
            return book != null ? Optional.of(book.toBuilder().build()) : Optional.empty();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public List<Book> findAll() {
        return findAll(null);
    }

    @Override
    public List<Book> findAll(Comparator<Book> comparator) {
        readLock.lock();
        try {
            var stream = storage.values().stream().map(b -> b.toBuilder().build());
            if (comparator != null) {
                stream = stream.sorted(comparator);
            }
            return stream.collect(Collectors.toList());
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public List<Book> findPaged(int pageNumber, int pageSize, Comparator<Book> comparator) {
        if (pageNumber < 1 || pageSize < 1) {
            throw new IllegalArgumentException("Page number and page size must be greater than zero");
        }
        readLock.lock();
        try {
            var stream = storage.values().stream().map(b -> b.toBuilder().build());
            if (comparator != null) {
                stream = stream.sorted(comparator);
            }
            return stream
                    .skip((long) (pageNumber - 1) * pageSize)
                    .limit(pageSize)
                    .collect(Collectors.toList());
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public List<Book> findByTitle(String titleQuery) {
        if (titleQuery == null || titleQuery.isBlank()) {
            return Collections.emptyList();
        }
        String q = titleQuery.trim().toLowerCase();

        readLock.lock();
        try {
            // First check inverted token index for fast match
            Set<String> tokenMatches = titleIndex.get(q);
            if (tokenMatches != null && !tokenMatches.isEmpty()) {
                return tokenMatches.stream()
                        .map(storage::get)
                        .filter(Objects::nonNull)
                        .map(b -> b.toBuilder().build())
                        .collect(Collectors.toList());
            }

            // Fallback to substring matching across storage values
            return storage.values().stream()
                    .filter(b -> b.getTitle() != null && b.getTitle().toLowerCase().contains(q))
                    .map(b -> b.toBuilder().build())
                    .collect(Collectors.toList());
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public List<Book> findByAuthor(String authorQuery) {
        if (authorQuery == null || authorQuery.isBlank()) {
            return Collections.emptyList();
        }
        String q = authorQuery.trim().toLowerCase();

        readLock.lock();
        try {
            // First check inverted token index for fast match
            Set<String> tokenMatches = authorIndex.get(q);
            if (tokenMatches != null && !tokenMatches.isEmpty()) {
                return tokenMatches.stream()
                        .map(storage::get)
                        .filter(Objects::nonNull)
                        .map(b -> b.toBuilder().build())
                        .collect(Collectors.toList());
            }

            // Fallback to substring matching across storage values
            return storage.values().stream()
                    .filter(b -> b.getAuthor() != null && b.getAuthor().toLowerCase().contains(q))
                    .map(b -> b.toBuilder().build())
                    .collect(Collectors.toList());
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean existsByIsbn(String isbn) {
        if (isbn == null) {
            return false;
        }
        String key = BookValidator.normalizeIsbn(isbn);

        readLock.lock();
        try {
            return storage.containsKey(key);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean update(Book book) {
        if (book == null || book.getIsbn() == null) {
            return false;
        }
        String key = BookValidator.normalizeIsbn(book.getIsbn());

        writeLock.lock();
        try {
            if (storage.containsKey(key)) {
                deindexBook(key);
                Book copy = book.toBuilder().build();
                storage.put(key, copy);
                indexBook(key, copy);
                return true;
            }
            return false;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean deleteByIsbn(String isbn) {
        if (isbn == null) {
            return false;
        }
        String key = BookValidator.normalizeIsbn(isbn);

        writeLock.lock();
        try {
            if (storage.remove(key) != null) {
                deindexBook(key);
                return true;
            }
            return false;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public int count() {
        readLock.lock();
        try {
            return storage.size();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void clear() {
        writeLock.lock();
        try {
            storage.clear();
            titleIndex.clear();
            authorIndex.clear();
        } finally {
            writeLock.unlock();
        }
    }

    // =========================================================================
    // Inverted Index Management (O(1) Token Keyword Indexing)
    // =========================================================================

    private void indexBook(String isbnKey, Book book) {
        if (book.getTitle() != null) {
            for (String word : tokenize(book.getTitle())) {
                titleIndex.computeIfAbsent(word, k -> new HashSet<>()).add(isbnKey);
            }
        }
        if (book.getAuthor() != null) {
            for (String word : tokenize(book.getAuthor())) {
                authorIndex.computeIfAbsent(word, k -> new HashSet<>()).add(isbnKey);
            }
        }
    }

    private void deindexBook(String isbnKey) {
        for (Set<String> isbns : titleIndex.values()) {
            isbns.remove(isbnKey);
        }
        for (Set<String> isbns : authorIndex.values()) {
            isbns.remove(isbnKey);
        }
    }

    private String[] tokenize(String text) {
        return text.toLowerCase().split("[^a-z0-9]+");
    }
}
