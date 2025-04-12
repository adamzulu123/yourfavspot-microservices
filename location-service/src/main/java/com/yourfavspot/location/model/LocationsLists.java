package com.yourfavspot.location.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDateTime;
import java.util.List;

//listy miejscówek
@Document(collection = "locationLists")
public class LocationsLists {
    @MongoId
    private String id;
    private String userId;
    private String name;
    private String description;
    private LocationVisibility visibility;
    private List<String> sharedWith;
    private List<String> locationIds;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}