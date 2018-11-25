package com.malt.places.loader.elasticsearch;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.SearchHit;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

@Slf4j
public class ActionListenerAdminAreaRetrieveInformations implements ActionListener<MultiSearchResponse> {

    private String geonameid;
    private Client client;
    private BulkProcessor bulkProcessor;

    public ActionListenerAdminAreaRetrieveInformations(String geonameid, Client client, BulkProcessor bulkProcessor) {
        this.geonameid = geonameid;
        this.client = client;
        this.bulkProcessor = bulkProcessor;
    }

    @Override
    public void onResponse(MultiSearchResponse items) {
        try {
            MultiSearchResponse.Item[] responses = items.getResponses();

            XContentBuilder doc = jsonBuilder().startObject();
            if (responses.length > 0){
                MultiSearchResponse.Item countryResponse = responses[0];
                SearchHit[] hits = countryResponse.getResponse().getHits().getHits();
                if (hits.length > 0) {
                    Map<String, Object> source = hits[0].getSource();
                    String countryName = (String) source.get("name");
                    List<Map<String, String>> countryAlternateNames = (List<Map<String, String>>) source.get("alternates");

                    doc = doc.startObject("country")
                            .field("name", countryName)
                            .field("alternates", countryAlternateNames)
                            .endObject();
                }

                if (responses.length > 1) {
                    MultiSearchResponse.Item admin1Response = responses[1];
                    hits = admin1Response.getResponse().getHits().getHits();
                    if (hits.length > 0) {
                        Map<String, Object>source = hits[0].getSource();
                        String admin1Name = (String) source.get("name");
                        List<Map<String, String>> admin1AlternateNames = (List<Map<String, String>>) source.get("alternates");

                        doc = doc.startObject("admin1")
                                .field("name", admin1Name)
                                .field("alternates", admin1AlternateNames)
                                .endObject();
                    }

                    if (responses.length > 2) {
                        MultiSearchResponse.Item admin2Response = responses[2];
                        hits = admin2Response.getResponse().getHits().getHits();
                        if (hits.length > 0) {
                            Map<String, Object> source = hits[0].getSource();
                            String admin2Name = (String) source.get("name");
                            List<Map<String, String>> admin2AlternateNames = (List<Map<String, String>>) source.get("alternates");

                            doc = doc.startObject("admin2")
                                    .field("name", admin2Name)
                                    .field("alternates", admin2AlternateNames)
                                    .endObject();
                        }
                    }
                }
                doc = doc.endObject();
                UpdateRequest update = new UpdateRequest("geonames", "location", geonameid).doc(doc);
                bulkProcessor.add(update);

            }

        } catch (IOException e) {
            log.error("error when updating admin2", e);
        }
    }

    @Override
    public void onFailure(Throwable e) {
        log.error("Error when retrieving admin area", e);
    }
}
