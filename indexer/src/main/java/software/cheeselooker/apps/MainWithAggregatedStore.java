package software.cheeselooker.apps;

import software.cheeselooker.control.IndexerCommand;
import software.cheeselooker.control.HazelcastConfig;
import software.cheeselooker.exceptions.IndexerException;
import software.cheeselooker.implementations.AggregatedHierarchicalCsvStore;
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

public class MainWithAggregatedStore {
    public static void main(String[] args) {
        // Path bookDatalakePath = Paths.get(System.getProperty("user.dir"), "/data/datalake");
        // Path invertedIndexPath = Paths.get(System.getProperty("user.dir"), "/data/datamart");
        Path stopWordsPath = Paths.get("/app/resources/stopwords.txt");

        // IndexerReader indexerReader = new GutenbergBookReader(bookDatalakePath.toString());
        // IndexerStore hierarchicalCsvStore = new AggregatedHierarchicalCsvStore(invertedIndexPath, stopWordsPath);

        // IndexerCommand hierarchicalCsvController = new IndexerCommand(indexerReader, hierarchicalCsvStore);

        HazelcastInstance hazelcastInstance = HazelcastConfig.getHazelcastInstance();
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