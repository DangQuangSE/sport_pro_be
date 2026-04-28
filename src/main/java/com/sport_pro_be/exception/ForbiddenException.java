package com.sport_pro_be.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends AppException {
    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
