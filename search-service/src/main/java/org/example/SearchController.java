package org.example;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.multimap.MultiMap;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
public class SearchController {

    private final HazelcastInstance hz;

    public SearchController(@Qualifier("searchHazelcast") HazelcastInstance hz) {
        this.hz = hz;
    }

    @GetMapping("/search")
    public Collection<String> search(@RequestParam("term") String term) {
        MultiMap<String, String> index = hz.getMultiMap("inverted-index");
        return index.get(term.toLowerCase());
    }
}