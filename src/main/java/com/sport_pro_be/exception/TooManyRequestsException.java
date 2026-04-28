package com.sport_pro_be.exception;

import org.springframework.http.HttpStatus;

public class TooManyRequestsException extends AppException {
    public TooManyRequestsException(String message) {
        super(message, HttpStatus.TOO_MANY_REQUESTS);
    }
}
