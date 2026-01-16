package org.example;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IndexerConfig {

    @Bean
    public HazelcastInstance hazelcastInstance() {

        // Crear configuración base de Hazelcast
        Config config = new Config();
        config.setClusterName("bd-search-cluster");

        // ACTIVAR AUTO-DESCUBRIMIENTO PARA EL CLUSTER
        JoinConfig join = config.getNetworkConfig().getJoin();
        join.getMulticastConfig().setEnabled(false);
        join.getTcpIpConfig().setEnabled(true)
                .addMember("indexer-service")
                .addMember("search1")
                .addMember("search2");

        // Configurar el índice invertido como MultiMap
        MapConfig invertedIndex = new MapConfig("inverted-index");
        invertedIndex.setBackupCount(1);  // 1 réplica
        config.addMapConfig(invertedIndex);

        // Crear e iniciar un nodo Hazelcast para este Indexer
        return Hazelcast.newHazelcastInstance(config);
    }
}
