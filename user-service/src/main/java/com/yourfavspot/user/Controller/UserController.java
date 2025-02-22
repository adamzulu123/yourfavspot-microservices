package com.yourfavspot.user.Controller;

import com.yourfavspot.user.Model.UserRegistrationRequest;
import com.yourfavspot.user.Service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("api/v1/user")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public void registerUser(@RequestBody UserRegistrationRequest registrationRequest) {
        log.info("Registering user: {}", registrationRequest);
        userService.registerUser(registrationRequest);
    }

    @PostMapping("/{userId}/favorite-locations")
    public ResponseEntity<String> addFavoriteLocation(@PathVariable("userId") Integer userId,
                                                      @RequestParam("locationId") String locationId) {
        userService.checkAndAddFavoriteLocation(userId, locationId);
        return ResponseEntity.ok("Request to check and add favorite location sent successfully");
    }

}
