package com.inventory.library.exception;

/**
 * Thrown when a book with a specified ISBN cannot be located in the inventory.
 */
public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(String message) {
        super(message);
    }
}
