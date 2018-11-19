package com.malt.places.api.service;

import com.malt.places.api.model.Geopoint;
import com.malt.places.api.model.Suggestion;
import com.malt.places.api.repository.SuggestRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.Locale;

@Service
public class SuggestService {

    @Inject
    private SuggestRepository suggestRepository;

    public List<Suggestion> suggest(String query, Geopoint geopoint, Locale locale) {
        return suggestRepository.suggest(query, geopoint, locale);
    }

}
