package com.yourfavspot.location.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@AllArgsConstructor
@Getter
@Setter
@Document(collection = "locations")
@NoArgsConstructor
public class LocationModel {

    @MongoId
    private String id;
    private Integer userId; //jeśli null to publiczna lokalizacja
    private boolean isPersonal; //takie upewnienie, że publiczna lub personalna
    private String name;
    private String description;
    private String category;
    @GeoSpatialIndexed
    private double[] location;
}
