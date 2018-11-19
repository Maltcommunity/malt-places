package com.malt.places.api.view;

import com.malt.places.api.model.Geopoint;
import lombok.Data;

import java.util.List;

@Data
public class SuggestResponse {

    private String administrativeAreaLevel1;
    private String administrativeAreaLevel1Code;
    private String administrativeAreaLevel2;
    private String administrativeAreaLevel2Code;
    private String administrativeAreaLevel3;
    private String administrativeAreaLevel3Code;
    private String administrativeAreaLevel4;
    private String administrativeAreaLevel4Code;
    private String country;
    private String countryCode;
    private String name;

    private String timezone;
    private Geopoint geopoint;
    private String geonameid;
    private List<String> zipCodes;
}
