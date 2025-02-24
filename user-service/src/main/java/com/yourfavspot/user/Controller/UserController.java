package com.yourfavspot.user.Controller;

import com.yourfavspot.common.AddLocationRequest;
import com.yourfavspot.user.Model.UserRegistrationRequest;
import com.yourfavspot.user.Service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

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

    //endpoint do dodawania istniejącej (np. publicznej lokalizacji do ulubionych)
    @PostMapping("/{userId}/favorite-locations")
    public ResponseEntity<String> addFavoriteLocation(@PathVariable("userId") Integer userId,
                                                      @RequestParam("locationId") String locationId) {
        userService.checkAndAddFavoriteLocation(userId, locationId);
        return ResponseEntity.ok("Request to check and add favorite location sent successfully");
    }

    //reactive adding new fav loc
    @PostMapping("/{userId}/reactive")
    public Mono<ResponseEntity<String>> addFavoriteLocationRec(@PathVariable("userId") Integer userId,
                                                               @RequestParam("locationId") String locationId){
        return userService.checkAndAddFavoriteLocationReactive(userId, locationId)
                .then(Mono.just(ResponseEntity.ok("Request to check and add favorite location sent successfully")));
    }

    //todo: reaktywnie do zrobienia - reactor rabbitmq
    //endpoint do dodawania nowej lokalizcji (personalnej użytkownika).
    @PostMapping("/{userId}/addLocation")
    public ResponseEntity<String> addPersonalLocation(@PathVariable("userId") Integer userId,
                                                      @RequestBody AddLocationRequest addLocationRequest) {
        log.info("Adding personal location: {} for user: {}", addLocationRequest, userId);
        userService.addNewLocation(userId, addLocationRequest);
        return ResponseEntity.ok("Request to add personal location sent successfully");
    }

}









