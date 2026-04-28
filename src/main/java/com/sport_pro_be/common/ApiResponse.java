package com.sport_pro_be.common;

import java.time.Instant;

public record ApiResponse<T>(String message, T data, Instant timestamp) {

    public static <T> ApiResponse<T> of(String message, T data) {
        return new ApiResponse<>(message, data, Instant.now());
    }

}
