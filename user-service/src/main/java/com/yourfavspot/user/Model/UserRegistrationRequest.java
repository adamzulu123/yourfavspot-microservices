package com.yourfavspot.user.Model;

public record UserRegistrationRequest(
        String firstName,
        String lastName,
        String email,
        String password) {
}
