package software.cheeselooker.apps;

import software.cheeselooker.control.IndexerCommand;
import software.cheeselooker.control.HazelcastConfig;
import software.cheeselooker.exceptions.IndexerException;
import software.cheeselooker.implementations.ExpandedHierarchicalCsvStore;
import software.cheeselooker.implementations.GutenbergBookReader;
import software.cheeselooker.ports.IndexerReader;
import software.cheeselooker.ports.IndexerStore;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;


public class MainWithExpandedStore {
    public static void main(String[] args) {
        Path stopWordsPath = Paths.get("/app/resources/stopwords.txt");
        
        HazelcastInstance hazelcastInstance = HazelcastConfig.getHazelcastInstance();
        System.out.println("Cluster members: " + hazelcastInstance.getCluster().getMembers());

        IndexerCommand hierarchicalCsvController = new IndexerCommand(hazelcastInstance);

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        scheduler.scheduleAtFixedRate(() -> {
            try {
                hierarchicalCsvController.execute();
            } catch (IndexerException e) {
                throw new RuntimeException("Error while indexing books.", e);
            }
        }, 0, 20, TimeUnit.MINUTES);
    }
}