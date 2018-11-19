package com.malt.places.api.view;

import lombok.Data;
import lombok.Value;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SuggestRequest {

    private String query;
    private Double lat;
    private Double lon;
    private String language;
}
