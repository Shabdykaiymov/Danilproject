package com.example.backend.RouteMate.payload.request;

public record PutRequest(
        String description,
        String startLocation,
        String endLocation

) {
}
