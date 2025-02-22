package com.yourfavspot.common;

public record LocationResponse(
        Integer userId,
        String locationId,
        boolean exists
) {
}
