package org.example;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SearchClusterConfig {

    @Bean
    public HazelcastInstance hazelcastInstance() {

        // Config Hazelcast
        Config config = new Config();
        config.setClusterName("bd-search-cluster");

        JoinConfig join = config.getNetworkConfig().getJoin();
        join.getMulticastConfig().setEnabled(false);
        join.getTcpIpConfig().setEnabled(true)
                .addMember("indexer-service")
                .addMember("search1")
                .addMember("search2");

        // Map config (inverted index)
        MapConfig mapConfig = new MapConfig("inverted-index");
        mapConfig.setBackupCount(1);
        config.addMapConfig(mapConfig);

        // Crear instancia Hazelcast
        return Hazelcast.newHazelcastInstance(config);
    }
}
