package com.malt.places.api.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class Geopoint {
    private Double lat;
    private Double lon;

    public Geopoint() {
    }

    public Geopoint(Double lat, Double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public boolean isValid() {
        return lat != null && lon != null;

    }
}
