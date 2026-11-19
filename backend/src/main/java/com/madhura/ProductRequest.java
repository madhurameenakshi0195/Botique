package com.madhura;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProductRequest(
        String name,
        String category,
        String subcategory,
        Double price,
        String description,
        @JsonProperty("image_url") String imageUrl,
        @JsonProperty("in_stock") Boolean inStock) {
}
