package com.yourfavspot.common;

public record AddLocationResponse(
        Integer userId,
        String locationId,
        boolean success,
        String message
) {
}
