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
        suggestResponse.setAdministrativeAreaLevel1Code(suggestion.getAdmin1().getCode());
        suggestResponse.setAdministrativeAreaLevel1(suggestion.getAdmin1().getName());
        suggestResponse.setAdministrativeAreaLevel2Code(suggestion.getAdmin2().getCode());
        suggestResponse.setAdministrativeAreaLevel2(suggestion.getAdmin2().getName());
        suggestResponse.setAdministrativeAreaLevel3Code(suggestion.getAdmin3().getCode());
        suggestResponse.setAdministrativeAreaLevel3(suggestion.getAdmin3().getName());
        suggestResponse.setAdministrativeAreaLevel4Code(suggestion.getAdmin4().getCode());
        suggestResponse.setAdministrativeAreaLevel4(suggestion.getAdmin4().getName());
        suggestResponse.setCountryCode(suggestion.getCountry().getCode());
        suggestResponse.setCountry(suggestion.getCountry().getName());
        suggestResponse.setGeonameid(suggestion.getGeonameid());
        suggestResponse.setGeopoint(suggestion.getGeo());
        suggestResponse.setTimezone(suggestion.getTimezone());
        suggestResponse.setZipCodes(suggestion.getZipCodes());
        suggestResponse.setName(suggestion.getName());
        return suggestResponse;
    }
}
