package org.example.ash.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class MissingHeaderException extends RuntimeException {
    public MissingHeaderException(String message) {
        super(message);
    }
}