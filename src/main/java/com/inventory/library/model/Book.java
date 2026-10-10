package com.inventory.library.model;

import java.util.Objects;

/**
 * Represents a Book entity within the Library Book Inventory System.
 * Encapsulates core metadata: ISBN, title, author, and publication year.
 */
public class Book {
    private String isbn;
    private String title;
    private String author;
    private int publicationYear;

    /**
     * Default constructor.
     */
    public Book() {
    }

    /**
     * Parameterized constructor to initialize a Book instance.
     *
     * @param isbn            International Standard Book Number (unique identifier)
     * @param title           Title of the book
     * @param author          Author of the book
     * @param publicationYear Year the book was published
     */
    public Book(String isbn, String title, String author, int publicationYear) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publicationYear = publicationYear;
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
     * Checks equality based on the unique ISBN.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(isbn, book.isbn);
    }

    /**
     * Generates hash code based on ISBN.
     */
    @Override
    public int hashCode() {
        return Objects.hash(isbn);
    }

    /**
     * Returns a formatted string representation of the book.
     */
    @Override
    public String toString() {
        return String.format("Book [ISBN: %-15s | Title: %-28s | Author: %-20s | Year: %4d]",
                isbn, title, author, publicationYear);
    }
}
