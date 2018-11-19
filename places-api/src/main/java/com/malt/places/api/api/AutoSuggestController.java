package com.malt.places.api.api;

import com.malt.places.api.view.SuggestRequest;
import com.malt.places.api.view.SuggestResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

@RestController
public class AutoSuggestController {

    @Inject
    private Api api;

    @GetMapping("/malt-places/suggest")
    public List<SuggestResponse> suggest(@RequestParam String query,
                                         @RequestParam(required = false, name = "lg") String language,
                                         @RequestParam(required = false) Double lat,
                                         @RequestParam(required = false) Double lon) {
        return api.suggest(new SuggestRequest()
                .setQuery(query)
                .setLat(lat)
                .setLon(lon)
                .setLanguage(language));
    }
}
