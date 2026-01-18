package io.cockroachdb.pest.util;

public class CommandException extends RuntimeException {
    public CommandException(String message, Throwable cause) {
        super(message, cause);
    }
}
