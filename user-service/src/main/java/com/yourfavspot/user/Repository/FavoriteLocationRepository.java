package com.yourfavspot.user.Repository;

import com.yourfavspot.user.Model.FavoriteLocation;
import com.yourfavspot.user.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteLocationRepository extends JpaRepository<FavoriteLocation, Integer> {
    List<FavoriteLocation> findByUserId(Integer userId);
}
