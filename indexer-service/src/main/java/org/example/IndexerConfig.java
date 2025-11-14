package org.example;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IndexerConfig {

    @Bean
    public Config hazelcastConfiguration() {
        Config config = new Config();
        config.setClusterName("search-cluster");

        MapConfig invertedIndex = new MapConfig("inverted-index");
        invertedIndex.setBackupCount(1);

        config.addMapConfig(invertedIndex);
        return config;
    }
}
