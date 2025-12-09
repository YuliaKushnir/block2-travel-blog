package org.example.block2travelblog.exception;

/**
 * Exception thrown when data is not saved in database
 */
public class CreationException extends RuntimeException {
    public CreationException(String message) { super(message); }
}
