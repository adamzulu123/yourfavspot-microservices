package com.yourfavspot.location.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@Document(collection = "locations")
@NoArgsConstructor
public class LocationModel {

    @MongoId
    private String id;
    private Integer userId; //jeśli null to publiczna lokalizacja - mówi kto utworzył lokalizację
    private LocationVisibility visibility;
    private List<Integer> sharedWith;
    private boolean isPersonal; //takie upewnienie, że publiczna lub personalna
    private String name;
    private String description;
    private String category;
    @GeoSpatialIndexed
    private double[] location;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

}
