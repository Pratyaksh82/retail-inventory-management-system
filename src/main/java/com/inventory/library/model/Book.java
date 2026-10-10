package com.inventory.library.model;

import java.util.Objects;

/**
 * Represents a Book entity within the Library Book Inventory System.
 * Refactored in Week 4 with the Builder Pattern to improve readability,
 * eliminate telescoping constructor anti-patterns, and enhance immutability options.
 */
public class Book {
    private String isbn;
    private String title;
    private String author;
    private int publicationYear;

    /**
     * Default constructor for serialization / JavaBean compatibility.
     */
    public Book() {
    }

    /**
     * Parameterized constructor.
     *
     * @param isbn            International Standard Book Number
     * @param title           Title of the book
     * @param author          Author of the work
     * @param publicationYear Year of publication
     */
    public Book(String isbn, String title, String author, int publicationYear) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publicationYear = publicationYear;
    }

    /**
     * Private constructor for Builder pattern.
     */
    private Book(Builder builder) {
        this.isbn = builder.isbn;
        this.title = builder.title;
        this.author = builder.author;
        this.publicationYear = builder.publicationYear;
    }

    /**
     * Creates a new Builder instance.
     *
     * @return A new Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a builder initialized with this Book's properties (toBuilder pattern).
     */
    public Builder toBuilder() {
        return new Builder()
                .isbn(this.isbn)
                .title(this.title)
                .author(this.author)
                .publicationYear(this.publicationYear);
    }

    // --- Getters and Setters ---

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(int publicationYear) {
        this.publicationYear = publicationYear;
    }

    /**
     * Equality contract based solely on the unique normalized ISBN.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        if (isbn == null || book.isbn == null) return false;
        String clean1 = isbn.replaceAll("[\\s-]", "").toUpperCase();
        String clean2 = book.isbn.replaceAll("[\\s-]", "").toUpperCase();
        return clean1.equals(clean2);
    }

    /**
     * HashCode based on normalized ISBN.
     */
    @Override
    public int hashCode() {
        if (isbn == null) return 0;
        return Objects.hash(isbn.replaceAll("[\\s-]", "").toUpperCase());
    }

    @Override
    public String toString() {
        return String.format("Book [ISBN: %-15s | Title: %-28s | Author: %-20s | Year: %4d]",
                isbn, title, author, publicationYear);
    }

    // =========================================================================
    // Builder Pattern Implementation
    // =========================================================================

    public static class Builder {
        private String isbn;
        private String title;
        private String author;
        private int publicationYear;

        public Builder isbn(String isbn) {
            this.isbn = isbn;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder author(String author) {
            this.author = author;
            return this;
        }

        public Builder publicationYear(int publicationYear) {
            this.publicationYear = publicationYear;
            return this;
        }

        public Book build() {
            return new Book(this);
        }
    }
}
