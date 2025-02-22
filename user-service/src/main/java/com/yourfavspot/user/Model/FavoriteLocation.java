package com.yourfavspot.user.Model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "favorite_locations")
public class FavoriteLocation { // Zmiana nazwy na pojedynczą formę, bo reprezentuje jeden rekord
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "location_id", nullable = false)
    private String locationId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
