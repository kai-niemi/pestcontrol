package io.cockroachdb.pest.web.api.cluster;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UploadException extends RuntimeException {
    public UploadException(String message) {
        super(message);
    }
}
