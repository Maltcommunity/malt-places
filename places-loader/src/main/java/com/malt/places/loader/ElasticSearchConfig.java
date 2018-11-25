package com.malt.places.loader;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;

@Configuration
public class ElasticSearchConfig {

    @Value(value = "${es.cluster.name:hopwork}")
    private String clusterName;

    @Value(value = "${es.port:9301}")
    private int esPort;

    @Value(value = "${es.nodes:localhost}")
    private String[] esNodes;

    @Bean
    public Client esClient() {
        Settings settings = Settings.settingsBuilder()
                .put("cluster.name", clusterName)
                .put("client.transport.ping_timeout", "10s")
                .put("client.transport.nodes_sampler_interval", "5s")
                .put("client.transport.sniff", true)
                .build();

        TransportClient client =  TransportClient.builder().settings(settings).build();

        for (int x = 0; x < esNodes.length; x++) {
            client.addTransportAddress(new InetSocketTransportAddress(new InetSocketAddress(esNodes[x], esPort)));
        }
        return client;
    }
}
