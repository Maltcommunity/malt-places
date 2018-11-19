package com.malt.places.api.api;

import com.malt.places.api.model.Geopoint;
import com.malt.places.api.model.Suggestion;
import com.malt.places.api.service.SuggestService;
import com.malt.places.api.view.SuggestRequest;
import com.malt.places.api.view.SuggestResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@Slf4j
public class Api {
    @Inject
    private SuggestService suggestService;

    public List<SuggestResponse> suggest(SuggestRequest query) {
        Geopoint geopoint = new Geopoint(query.getLat(), query.getLon());
        Locale locale = query.getLanguage() != null ? Locale.forLanguageTag(query.getLanguage()) : Locale.ENGLISH;
        List<SuggestResponse> suggestions = suggestService.suggest(query.getQuery(), geopoint, locale).stream().map(this::toSuggestResponse).collect(Collectors.toList());



        return suggestions;
    }

    private SuggestResponse toSuggestResponse(Suggestion suggestion) {
        log.info("" + suggestion);
        SuggestResponse suggestResponse = new SuggestResponse();
        suggestResponse.setAdministrativeAreaLevel1Code(suggestion.getAdministrativeAreaLevel1Code());
        suggestResponse.setAdministrativeAreaLevel2Code(suggestion.getAdministrativeAreaLevel2Code());
        suggestResponse.setAdministrativeAreaLevel3Code(suggestion.getAdministrativeAreaLevel3Code());
        suggestResponse.setAdministrativeAreaLevel4Code(suggestion.getAdministrativeAreaLevel4Code());
        suggestResponse.setCountryCode(suggestion.getCountryCode());
        suggestResponse.setCountry(new Locale("", suggestion.getCountryCode()).getDisplayName());
        suggestResponse.setGeonameid(suggestion.getGeonameid());
        suggestResponse.setGeopoint(suggestion.getGeo());
        suggestResponse.setTimezone(suggestion.getTimezone());
        suggestResponse.setZipCodes(suggestion.getZipCodes());
        suggestResponse.setName(suggestion.getName());
        return suggestResponse;
    }
}
