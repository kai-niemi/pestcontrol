package io.cockroachdb.pestcontrol.web.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.I_AM_A_TEAPOT)
public class FakeException extends RuntimeException {
    public FakeException(String message) {
        super(message);
    }

    public FakeException(String message, Throwable cause) {
        super(message, cause);
    }
}