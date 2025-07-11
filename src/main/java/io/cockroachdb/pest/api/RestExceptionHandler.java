package io.cockroachdb.pest.api;

import java.lang.reflect.UndeclaredThrowableException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

// Have this exception handler catch all errors unless they originate from the web frontend.
@RestControllerAdvice(annotations = RestController.class)
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ResponseEntity<Object> wrap(Problem problem) {
        ProblemDetail problemDetail = ProblemDetail
                .forStatusAndDetail(problem.getStatus(), problem.getDetail());
        problemDetail.setTitle(problem.getTitle());
        problemDetail.setInstance(problem.getInstance());
        return ResponseEntity
                .status(problem.getStatus())
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problemDetail);
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handleAny(Throwable ex) {
        if (ex instanceof UndeclaredThrowableException) {
            ex = ((UndeclaredThrowableException) ex).getUndeclaredThrowable();
        }

        logger.error("Server error processing request", ex);

        ResponseStatus responseStatus = AnnotationUtils.findAnnotation(ex.getClass(), ResponseStatus.class);

        return wrap(Problem.create()
                .withTitle(ex.getLocalizedMessage())
                .withDetail(NestedExceptionUtils.getMostSpecificCause(ex).toString())
                .withStatus(responseStatus != null
                        ? responseStatus.value() : HttpStatus.INTERNAL_SERVER_ERROR));
    }
}

