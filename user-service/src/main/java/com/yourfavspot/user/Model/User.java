package com.yourfavspot.user.Model;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @SequenceGenerator(
            name="user_id_sequence",
            sequenceName = "user_id_sequence"
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "user_id_sequence"
    )
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;

}
