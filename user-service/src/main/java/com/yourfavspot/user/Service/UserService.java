package com.yourfavspot.user.Service;

import com.yourfavspot.user.Model.User;
import com.yourfavspot.user.Model.UserRegistrationRequest;
import org.springframework.stereotype.Service;

@Service
public record UserService() {
    public static void registerUser(UserRegistrationRequest request) {
        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(request.password())
                .build();
        // todo: email (not taken?) and password valid? store customer in db
    }
}
