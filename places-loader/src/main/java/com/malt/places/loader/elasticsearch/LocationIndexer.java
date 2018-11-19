package com.malt.places.loader.elasticsearch;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

@Service
public class LocationIndexer {

    public void scheduleForInsertion(String[] r, BulkProcessor bulkProcessor) throws IOException {
        XContentBuilder doc = jsonBuilder()
                .startObject()
                .field("geonameid", r[0])
                .field("name", r[1])
                .field("feature_class", r[6])
                .field("feature_code", r[7])
                .startObject("country")
                .field("code", r[8])
                .endObject()
                .startObject("admin1")
                .field("code", r[10])
                .endObject()
                .startObject("admin1")
                .field("code", r[10])
                .endObject()
                .startObject("admin2")
                .field("code", r[11])
                .endObject()
                .startObject("admin3")
                .field("code", r[12])
                .endObject()
                .startObject("admin4")
                .field("code", r[13])
                .endObject()
                .field("timezone", r[17])
                .startArray("postal_code")
                .endArray()
                .startArray("alternates")
                .endArray();
        ;

        if (r[4] != null && r[5] != null && NumberUtils.isCreatable(r[4]) && NumberUtils.isCreatable(r[5])) {
            doc = doc.startObject("geo")
                    .field("lat",  NumberUtils.toDouble(r[4]))
                    .field("lon",  NumberUtils.toDouble(r[5]))
                    .endObject();
        }
        if (r[14] != null && NumberUtils.isDigits(r[14])) {
            doc = doc.field("population", NumberUtils.toLong(r[14]));
        }

        doc = doc.endObject();

        if (isAdministrativeAreaOrPopulatedPlaces(r) && isRelevantPlace(r)) {
            bulkProcessor.add(new IndexRequest("geonames", "location", r[0]).source(doc));
        }
    }


    private boolean isRelevantPlace(String[] r) {
        return !ImmutableList.of("PPLW", "PPLX", "PPLH", "PPLQ", "PRSH").contains(r[7]);
    }

    private boolean isAdministrativeAreaOrPopulatedPlaces(String[] r) {
        return r[6].equalsIgnoreCase("A") || r[6].equalsIgnoreCase("P");
    }

    public void schedulePostCodeUpdate(String[] record, BulkProcessor bulkProcessor) {
        String type = record[2];
        String zipCode = record[3];
        if (recordIsPostalCode(type, zipCode)) {
            Map<String, Object> parameters = singletonMap("zip", record[3]);
            Script inline = new Script("ctx._source.postal_code+=zip", ScriptService.ScriptType.INLINE, null, parameters);
            UpdateRequest script = new UpdateRequest("geonames", "location", record[1]).script(inline);
            bulkProcessor.add(script);
        }
    }

    private boolean recordIsPostalCode(String type, String zipCode) {
        return type.equalsIgnoreCase("post") &&  !zipCode.contains("CEDEX" );
    }

    public void scheduleAddAlternateName(String[] record, BulkProcessor bulkProcessor) throws IOException {
        String type = record[2];
        if (ImmutableList.of("fr", "en", "es", "it", "de", "pt", "nl").contains(type) && !"1".equalsIgnoreCase(record[5]) && !"1".equalsIgnoreCase(record[7]) ) {
            Map<String, Object> parameters = new HashMap<>();
            Map<String, Object> alternate = new HashMap<>();
            alternate.put("lang", type);
            alternate.put("name", record[3]);
            parameters.put("alternate", alternate);

            Script inline = new Script("ctx._source.alternates+=alternate", ScriptService.ScriptType.INLINE, null, parameters);
            UpdateRequest script = new UpdateRequest("geonames", "location", record[1]).script(inline);
            bulkProcessor.add(script);
        }
    }
}
