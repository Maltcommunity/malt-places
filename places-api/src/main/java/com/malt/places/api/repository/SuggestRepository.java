package com.malt.places.api.repository;

import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.malt.places.api.model.Geopoint;
import com.malt.places.api.model.Suggestion;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.lucene.search.function.FieldValueFactorFunction;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders.fieldValueFactorFunction;
import static org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders.gaussDecayFunction;

@Repository
@Slf4j
public class SuggestRepository {
    @Inject
    private Client client;
    @Inject
    private ObjectMapper mapper;

    public List<Suggestion> suggest(String query, Geopoint geopoint, Locale locale) {
        FunctionScoreQueryBuilder queryBuilder = functionScoreQuery(
                boolQuery()
                    .mustNot(rangeQuery("population").lte(200))
                    .mustNot(matchQuery("feature_code", "ADM3"))
                    .mustNot(matchQuery("feature_code", "ADM4"))
                    .mustNot(matchQuery("feature_code", "ADM5"))
                    .mustNot(matchQuery("feature_code", "ADMD"))
                    .mustNot(matchQuery("feature_code", "ADMDH"))
                    .mustNot(matchQuery("feature_code", "ADM1H"))
                    .mustNot(matchQuery("feature_code", "PPLX"))
                    .should(matchQuery("name.edgengram", query).operator(MatchQueryBuilder.Operator.AND))
                    .should(matchQuery("postal_code.edgengram", query).operator(MatchQueryBuilder.Operator.AND))
        ).add(fieldValueFactorFunction("population").modifier(FieldValueFactorFunction.Modifier.LOG1P));

        if (geopoint.isValid()) {
            queryBuilder.add(gaussDecayFunction("geo", new GeoPoint(
                    geopoint.getLat(),
                    geopoint.getLon()), "500km").setOffset("100km"));
        }

        SearchRequestBuilder searchRequestBuilder = client.prepareSearch("geonames")
                .setTypes("location")
                .setSize(10)
                .setQuery(queryBuilder);

        SearchResponse response = searchRequestBuilder.execute().actionGet();
        return toSuggestions(response);
    }

    private List<Suggestion> toSuggestions(SearchResponse result) {
        List<Suggestion> suggestions = new ArrayList<>();
        if (result.getHits().getHits().length != 0) {
            parseResults(result, suggestions);
        }
        return suggestions;
    }

    private void parseResults(SearchResponse result, List<Suggestion> suggestions) {
        try {
            for (SearchHit hits : result.getHits().getHits()) {
                suggestions.add(mapper.readValue(hits.getSourceAsString(), Suggestion.class));
            }
        } catch (IOException e) {
            log.error("unable to parse result from elastic search", e);
        }
    }
}
