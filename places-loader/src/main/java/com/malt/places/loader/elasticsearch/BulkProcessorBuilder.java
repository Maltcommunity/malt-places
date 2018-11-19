package com.malt.places.loader.elasticsearch;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.*;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;

@Slf4j
public class BulkProcessorBuilder {

    public static BulkProcessor build(Client client) {
        return  BulkProcessor.builder(
                client,
                new BulkProcessor.Listener() {
                    @Override
                    public void beforeBulk(long executionId,
                                           BulkRequest request) {
                    }

                    @Override
                    public void afterBulk(long executionId,
                                          BulkRequest request,
                                          BulkResponse response) {
                        if (response.hasFailures()) {
                            for (BulkItemResponse bulkItemResponse : response.getItems()) {
                                if (bulkItemResponse.isFailed() && bulkItemResponse.getFailureMessage().contains("DocumentMissingException")) {
                                    // do nothing
                                } else if (bulkItemResponse.isFailed()) {
                                    log.error(bulkItemResponse.getFailureMessage());
                                }
                            }
                        }
                    }
                    @Override
                    public void afterBulk(long executionId,
                                          BulkRequest request,
                                          Throwable failure) {
                        log.error("Error when loading data", failure);
                    }
                })
                .setBulkActions(10000)
                .setBulkSize(new ByteSizeValue(1, ByteSizeUnit.MB))
                .setFlushInterval(TimeValue.timeValueSeconds(20))
                .setConcurrentRequests(5)
                .setBackoffPolicy(
                        BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), 3))
                .build();
    }

}
