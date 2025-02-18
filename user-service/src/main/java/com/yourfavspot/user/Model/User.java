package com.yourfavspot.user.Model;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@Getter
@Setter
public class User {
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;

}
