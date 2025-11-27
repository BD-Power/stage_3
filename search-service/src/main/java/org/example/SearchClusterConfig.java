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
        config.setClusterName("search-cluster");

        // Map config (inverted index)
        MapConfig mapConfig = new MapConfig("inverted-index");
        mapConfig.setBackupCount(1);
        config.addMapConfig(mapConfig);

        // AUTO-DISCOVERY: activar multicast para descubrir los indexers y otros search nodes
        JoinConfig join = config.getNetworkConfig().getJoin();
        join.getMulticastConfig().setEnabled(true);
        join.getTcpIpConfig().setEnabled(false);

        // Crear instancia Hazelcast
        return Hazelcast.newHazelcastInstance(config);
    }
}
