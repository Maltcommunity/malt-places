package com.malt.places.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Suggestion {

    private AdminAreaDescription admin1;
    private AdminAreaDescription admin2;
    private AdminAreaDescription admin3;
    private AdminAreaDescription admin4;
    private AdminAreaDescription country;
    private String name;
    private String timezone;
    private Geopoint geo;
    private long population;
    private String geonameid;
    @JsonProperty("postal_code")
    private List<String> zipCodes;
    @JsonProperty("feature_code")
    private String featureCode;
    @JsonProperty("feature_class")
    private String featureClass;

}
