package com.yourfavspot.common;

public record NotificationRequest(
        Integer toCustomerId,
        String toCustomerName,
        String message
) {
}