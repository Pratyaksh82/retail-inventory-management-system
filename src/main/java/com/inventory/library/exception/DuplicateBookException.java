package com.inventory.library.exception;

/**
 * Thrown when attempting to add a book with an ISBN that already exists in the inventory.
 */
public class DuplicateBookException extends RuntimeException {
    public DuplicateBookException(String message) {
        super(message);
    }
}
