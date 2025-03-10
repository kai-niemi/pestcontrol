package io.cockroachdb.pc.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends ClusterException {
    public ResourceNotFoundException(String id) {
        super(id);
    }
}
