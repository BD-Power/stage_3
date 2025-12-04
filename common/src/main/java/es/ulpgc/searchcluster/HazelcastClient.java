package es.ulpgc.searchcluster;

import com.hazelcast.config.ClasspathYamlConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class HazelcastClient {

    private static HazelcastInstance instance;

    public static HazelcastInstance getInstance() {
        if (instance == null) {
            instance = Hazelcast.newHazelcastInstance(
                    new ClasspathYamlConfig("hazelcast.yaml")
            );
        }
        return instance;
    }
}
