package org.example.block2travelblog.exception;

/**
 * Exception thrown when attempting to create a user with a duplicate email field.
 */
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String message) { super(message); }

    public DuplicateEmailException(String message, Throwable cause) { super(message, cause); }

}
