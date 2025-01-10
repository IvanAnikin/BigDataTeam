package software.cheeselooker.apps;

import software.cheeselooker.control.Command;
import software.cheeselooker.control.CrawlerCommand;
import software.cheeselooker.control.HazelcastConfig;

import software.cheeselooker.implementations.ReaderFromWeb;
import software.cheeselooker.implementations.StoreInDatalake;
import software.cheeselooker.ports.ReaderFromWebInterface;
import software.cheeselooker.ports.StoreInDatalakeInterface;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;


public class Main {
    public static void main(String[] args) {

        HazelcastInstance hazelcastInstance = new HazelcastConfig().getHazelcastInstance();

        ReaderFromWebInterface reader = new ReaderFromWeb();
        StoreInDatalakeInterface store = new StoreInDatalake(hazelcastInstance);
        
        Command crawlerCommand = new CrawlerCommand(reader, store, hazelcastInstance);


        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        periodicTask(scheduler, crawlerCommand);
    }

    private static void periodicTask(ScheduledExecutorService scheduler, Command crawlerCommand) {
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("Starting download process...");
            crawlerCommand.download();
        }, 0, 20, TimeUnit.SECONDS);
    }

}
