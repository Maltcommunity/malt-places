package com.malt.places.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Suggestion {


    @JsonProperty("admin1_code")
    private String administrativeAreaLevel1Code;
    @JsonProperty("admin2_code")
    private String administrativeAreaLevel2Code;
    @JsonProperty("admin3_code")
    private String administrativeAreaLevel3Code;
    @JsonProperty("admin4_code")
    private String administrativeAreaLevel4Code;
    @JsonProperty("country_code")
    private String countryCode;
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
