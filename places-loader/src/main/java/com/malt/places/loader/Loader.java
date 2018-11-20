package com.malt.places.loader;

import com.malt.places.loader.elasticsearch.ActionListenerAdminAreaRetrieveInformations;
import com.malt.places.loader.elasticsearch.AdminIndexCreator;
import com.malt.places.loader.elasticsearch.BulkProcessorBuilder;
import com.malt.places.loader.elasticsearch.LocationIndexer;
import com.opencsv.CSVReader;
import com.opencsv.RFC4180Parser;
import com.opencsv.RFC4180ParserBuilder;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.search.MultiSearchRequestBuilder;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.elasticsearch.index.query.QueryBuilders.*;

@SpringBootApplication
@Slf4j
public class Loader implements ApplicationRunner {
    public static void main( String[] args )
    {
        SpringApplication.run(Loader.class, args);
    }

    @Inject
    Client client;
    @Inject
    LocationIndexer locationIndexer;
    @Inject
    AdminIndexCreator adminIndexCreator;
    @Inject
    FileManager fileManager;

    @Override
    public void run(ApplicationArguments args) throws IOException, ZipException, InterruptedException, ExecutionException {
        // check if files exists or download it
        String tempDir = System.getProperty("java.io.tmpdir");
        log.info("Working folder =  " + tempDir);

        fileManager.checkFileExistOrDownloadItAndUnzipIt(tempDir, "alternateNamesV2");
        fileManager.checkFileExistOrDownloadItAndUnzipIt(tempDir, "allCountries");
//
//        adminIndexCreator.createIndexQuietly();
//        loadData();
//        enrichDataWithPostCode();
        enrichDataWithNames();

    }

    private void enrichDataWithNames() throws InterruptedException, ExecutionException {
        SearchResponse scrollResp = client.prepareSearch("geonames")
                .addSort("_doc", SortOrder.ASC)
                .setScroll(new TimeValue(60000))
                .setQuery(QueryBuilders.matchAllQuery())
                .setSize(100).get();

        long count = 0;

        do {
            for (SearchHit hit : scrollResp.getHits().getHits()) {
                Map<String, Object> source = hit.getSource();

                String featureClass = (String) source.get("feature_class");
                String featureCode = (String) source.get("feature_code");

                if (featureClass.equalsIgnoreCase("A")) {
                    switch (featureCode) {
                        case "ADM2":

                            Map<String, String> country = (Map<String, String>) source.get("country");
                            Map<String, String> admin1 = (Map<String, String>) source.get("admin1");
                            BoolQueryBuilder queryToFindAdmin1 = boolQuery()
                                    .must(matchQuery("country.code", country.get("code")))
                                    .must(matchQuery("admin1.code", admin1.get("code")))
                                    .must(matchQuery("feature_code", "adm1"));

                            BoolQueryBuilder queryToFindCountry = boolQuery()
                                    .must(matchQuery("country.code", country.get("code")))
                                    .must(prefixQuery("feature_code", "pcl"));

                            SearchRequestBuilder countryQuery= client
                                    .prepareSearch("geonames").setQuery(queryToFindCountry).setSize(1);
                            SearchRequestBuilder admin1Country = client
                                    .prepareSearch("geonames").setQuery(queryToFindAdmin1).setSize(1);

                            MultiSearchResponse items = client.prepareMultiSearch()
                                    .add(countryQuery)
                                    .add(admin1Country)
                                    .execute().get();

                            ActionListenerAdminAreaRetrieveInformations listener = new ActionListenerAdminAreaRetrieveInformations((String) source.get("geonameid"), client, null);
                            listener.onResponse(items);

                            break;
                        case "ADM1":
                            country = (Map<String, String>) source.get("country");

                            queryToFindCountry = boolQuery()
                                    .must(matchQuery("country.code", country.get("code")))
                                    .must(prefixQuery("feature_code", "pcl"));

                            countryQuery= client
                                    .prepareSearch("geonames").setQuery(queryToFindCountry).setSize(1);
                            items =  client.prepareMultiSearch()
                                    .add(countryQuery)
                                    .execute().get();

                            listener = new ActionListenerAdminAreaRetrieveInformations((String) source.get("geonameid"), client, null);
                            listener.onResponse(items);

                            break;
                        default:
                            // do nothing
                            break;
                    }
                } else if (featureClass.equalsIgnoreCase("P")) {
                    Map<String, String> country = (Map<String, String>) source.get("country");
                    Map<String, String> admin1 = (Map<String, String>) source.get("admin1");
                    Map<String, String> admin2 = (Map<String, String>) source.get("admin2");
                    BoolQueryBuilder queryToFindAdmin2 = boolQuery()
                            .must(matchQuery("country.code", country.get("code")))
                            .must(matchQuery("admin1.code", admin1.get("code")))
                            .must(matchQuery("admin2.code", admin2.get("code")))
                            .must(matchQuery("feature_code", "adm2"));

                    BoolQueryBuilder queryToFindAdmin1 = boolQuery()
                            .must(matchQuery("country.code", country.get("code")))
                            .must(matchQuery("admin1.code", admin1.get("code")))
                            .must(matchQuery("feature_code", "adm1"));

                    BoolQueryBuilder queryToFindCountry = boolQuery()
                            .must(matchQuery("country.code", country.get("code")))
                            .must(prefixQuery("feature_code", "pcl"));

                    SearchRequestBuilder countryQuery= client
                            .prepareSearch("geonames").setQuery(queryToFindCountry).setSize(1);
                    SearchRequestBuilder admin1Query = client
                            .prepareSearch("geonames").setQuery(queryToFindAdmin1).setSize(1);
                    SearchRequestBuilder admin2Query = client
                            .prepareSearch("geonames").setQuery(queryToFindAdmin2).setSize(1);
                    MultiSearchRequestBuilder requestBuilder = client.prepareMultiSearch()
                            .add(countryQuery)
                            .add(admin1Query);

                    if (!StringUtils.isEmpty(admin2.get("code"))) {
                        requestBuilder = requestBuilder.add(admin2Query);
                    }
                    MultiSearchResponse items = requestBuilder.execute().get();
                    ActionListenerAdminAreaRetrieveInformations listener = new ActionListenerAdminAreaRetrieveInformations((String) source.get("geonameid"), client, null);
                    listener.onResponse(items);
                }
            }
            count++;

            if (count % 10000 == 0) {
                log.info("Elements processed : " + count);
            }

            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
        } while(scrollResp.getHits().getHits().length != 0);
    }

    private void enrichDataWithPostCode() throws IOException, InterruptedException {
        String tempDir = System.getProperty("java.io.tmpdir");
        String txtFile = tempDir + File.separator + "alternateNamesV2.txt";
        Reader reader = Files.newBufferedReader(Paths.get(txtFile));
        RFC4180Parser parser = new RFC4180ParserBuilder()
                .withQuoteChar('¤')
                .withSeparator('\t')
                .build();
        CSVReader csvReader = new CSVReader(reader, 0, parser);

        BulkProcessor bulkProcessor = BulkProcessorBuilder.build(client);

        log.info("loading postcode and alternate names...");
        String[] r;
        while ((r = csvReader.readNext()) != null) {
            try {
                locationIndexer.schedulePostCodeUpdate(r, bulkProcessor);
                locationIndexer.scheduleAddAlternateName(r, bulkProcessor);
            } catch (Exception e) {
                log.error("Error when parsing data ", e);
            }
        }

        bulkProcessor.flush();
        boolean terminated = bulkProcessor.awaitClose(30L, TimeUnit.SECONDS);

        if(!terminated) {
            log.warn("Some requests have not been processed");
        }
    }

    private void loadData() throws IOException, InterruptedException {
        String tempDir = System.getProperty("java.io.tmpdir");
        String txtFile = tempDir + File.separator + "allCountries.txt";
        Reader reader = Files.newBufferedReader(Paths.get(txtFile));
        RFC4180Parser parser = new RFC4180ParserBuilder()
                .withQuoteChar('¤')
                .withSeparator('\t')
                .build();
        CSVReader csvReader = new CSVReader(reader, 0, parser);

        BulkProcessor bulkProcessor = BulkProcessorBuilder.build(client);

        log.info("loading data...");
        String[] r;
        while ((r = csvReader.readNext()) != null) {
            try {
                locationIndexer.scheduleForInsertion(r, bulkProcessor);
            } catch (Exception e) {
                log.error("Error when loading data ", e);
            }
        }

        bulkProcessor.flush();
        boolean terminated = bulkProcessor.awaitClose(30L, TimeUnit.SECONDS);

        if(!terminated) {
            log.warn("Some requests have not been processed");
        }
    }



}
