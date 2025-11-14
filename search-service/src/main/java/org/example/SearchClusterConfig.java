package org.example;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SearchClusterConfig {

    @Bean
    public Config hazelcastConfig() {
        Config config = new Config();
        config.setClusterName("search-cluster");

        MapConfig mapConfig = new MapConfig("inverted-index");
        mapConfig.setBackupCount(1);
        config.addMapConfig(mapConfig);

        config.getNetworkConfig()
                .getJoin()
                .getMulticastConfig().setEnabled(false);

        config.getNetworkConfig()
                .getJoin()
                .getTcpIpConfig().setEnabled(true)
                .addMember("127.0.0.1");

        return config;
    }
}
