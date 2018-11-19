package com.malt.places.loader.elasticsearch;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;

@Service
@Slf4j
public class AdminIndexCreator {

    @Inject
    Client client;
    @Value("classpath:es-config.json")
    Resource resourceFile;

    public void createIndexQuietly() throws IOException {
        deleteIndexIfExist();
        createIndex();
    }

    private void createIndex() throws IOException {
        log.info("creating index geonames...");
        CreateIndexRequest createIndexRequest = new CreateIndexRequest("geonames");
        createIndexRequest.source(FileUtils.readFileToByteArray(resourceFile.getFile()));
        client.admin().indices().create(createIndexRequest).actionGet();
    }

    private void deleteIndexIfExist() {
        IndicesExistsResponse response = client.admin().indices().exists(new IndicesExistsRequest("geonames")).actionGet();
        if (response.isExists()) {
            log.info("Remove existing index geonames");
            client.admin().indices().delete(new DeleteIndexRequest("geonames")).actionGet();
        }
    }

}
