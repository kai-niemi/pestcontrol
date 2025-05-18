package io.cockroachdb.pestcontrol.web.api.toxi;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ToxiproxyAccessException extends RuntimeException {
    public ToxiproxyAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
