package com.yourfavspot.common;

public record AddLocationRequest(
        Integer userId,
        String name,
        String description,
        String type,
        double[] coordinates
) {}
