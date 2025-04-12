package com.yourfavspot.user.Controller;

import com.yourfavspot.common.AddLocationRequest;
import com.yourfavspot.user.Model.LoginRequest;
import com.yourfavspot.user.Model.LoginResponse;
import com.yourfavspot.user.Model.UserDto;
import com.yourfavspot.user.Service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("api/v1/user")
public class UserController {

    private final UserService userService;

    @PostMapping("/registerDto")
    public ResponseEntity<String> registerUser(@RequestBody UserDto userDto) {
        //boolean success = userService.registerUserDto(userDto);
        boolean success = userService.registerUserDto(userDto);
        if (success) {
            return ResponseEntity.ok("User registered successfully");
        }
        else{
            return ResponseEntity.status(500).body("Registration failed");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = userService.loginUser(loginRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Login failed", e);
            return ResponseEntity.status(401).build();
        }
    }



    /*
    @PostMapping("/register")
    public void registerUser(@RequestBody UserRegistrationRequest registrationRequest) {
        log.info("Registering user: {}", registrationRequest);
        userService.registerUser(registrationRequest);
    }

     */


    //reactive adding new fav loc
    @PostMapping("/{userId}/addFavoriteLocation")
    public Mono<ResponseEntity<String>> addFavoriteLocationRec(@PathVariable("userId") Integer userId,
                                                               @RequestParam("locationId") String locationId){
        return userService.checkAndAddFavoriteLocationReactive(userId, locationId)
                .then(Mono.just(ResponseEntity.ok("Request to check and add favorite location sent successfully")));
    }

    //endpoint do dodawania nowej lokalizcji (personalnej użytkownika) -reaktywnie
    @PostMapping("/{userId}/addLocation")
    public Mono<ResponseEntity<String>> addPersonalLocation(@PathVariable("userId") Integer userId,
                                                            @RequestBody AddLocationRequest addLocationRequest){
        return userService.addNewLocationReactive(userId, addLocationRequest)
                .then(Mono.just(ResponseEntity.ok("Request to add location sent successfully")));
    }

    /*
    // dwa aktualnie nie aktywne endpointy wczesniej uzywane do komunikacji synchronicznej

    public ResponseEntity<String> addPersonalLocation(@PathVariable("userId") Integer userId,
                                                      @RequestBody AddLocationRequest addLocationRequest) {
        log.info("Adding personal location: {} for user: {}", addLocationRequest, userId);
        userService.addNewLocation(userId, addLocationRequest);
        return ResponseEntity.ok("Request to add personal location sent successfully");
    }
    //endpoint do dodawania istniejącej (np. publicznej lokalizacji do ulubionych)
    @PostMapping("/{userId}/favorite-locations")
    public ResponseEntity<String> addFavoriteLocation(@PathVariable("userId") Integer userId,
                                                      @RequestParam("locationId") String locationId) {
        userService.checkAndAddFavoriteLocation(userId, locationId);
        return ResponseEntity.ok("Request to check and add favorite location sent successfully");
    }
     */

}









