package com.inventory.library.exception;

/**
 * Thrown when book input data violates domain validation rules
 * (e.g., blank title/author, malformed ISBN, or out-of-range publication year).
 */
public class InvalidBookDataException extends RuntimeException {
    public InvalidBookDataException(String message) {
        super(message);
    }
}
